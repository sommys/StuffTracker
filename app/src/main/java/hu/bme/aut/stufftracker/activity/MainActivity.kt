package hu.bme.aut.stufftracker.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.adapter.AddressListAdapter
import hu.bme.aut.stufftracker.adapter.AddressPagerAdapter
import hu.bme.aut.stufftracker.adapter.CategoryListAdapter
import hu.bme.aut.stufftracker.adapter.StuffListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.ActivityMainBinding
import hu.bme.aut.stufftracker.dialog.ChangePINDialog
import hu.bme.aut.stufftracker.dialog.NewAddressDialog
import hu.bme.aut.stufftracker.dialog.NewCategoryDialog
import hu.bme.aut.stufftracker.dialog.UpdatePictureDialog
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import hu.bme.aut.stufftracker.fragment.AddressFragment
import permissions.dispatcher.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


@RuntimePermissions
class MainActivity : AppCompatActivity(),
    AddressListAdapter.AddressItemListener,
    CategoryListAdapter.CategoryItemListener,
    NewAddressDialog.NewAddressDialogListener,
    NewCategoryDialog.NewCategoryDialogListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var addressPagerAdapter: AddressPagerAdapter
    private lateinit var navController: NavController
    private lateinit var db : StuffDatabase

    private var latestHolder: StuffListAdapter.StuffViewHolder? = null
    private var latestStuff: Stuff? = null

    private lateinit var currentPhotoPath: String

    private val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "stuffTracker")
    private val sdtf = SimpleDateFormat("yyyyMMdd_HHmmss")
    private val NOTFOUND: String = "NOTFOUND"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        db = StuffDatabase.getDatabase(this)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.manageAddressMenuItem -> {
                navController.navigate(R.id.addressManagerFragment)
            }
            R.id.manageCategoryMenuItem -> {
                navController.navigate(R.id.categoryManagerFragment)
            }
            R.id.changePINMenuItem -> {
                val dialog = ChangePINDialog()
                dialog.show(supportFragmentManager, "CHANGEPIN_DIALOG")
            }
            else -> { Toast.makeText(this, "Invalid Menu Item!", Toast.LENGTH_SHORT).show()}
        }
        return true
    }

    override fun onDestroy() {
        clearUpImages()
        db.close()
        super.onDestroy()
    }

    fun refreshFragment(addressId: Long) {
        for(af: AddressFragment in addressPagerAdapter.addressFragments){
            if(af.address.id!! == addressId){
                af.refreshNeeded = true
                return
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "Hiba történt a kép választás/készítés során...", Toast.LENGTH_SHORT).show()
            return
        }
        when(requestCode) {
            UpdatePictureDialog.REQUEST_CAMERA_IMAGE -> {
                val img = data!!.extras!!["data"] as Bitmap
                Log.d("IMG_BYTECOUNT", img.allocationByteCount.toString())
                if (img.allocationByteCount > 4 * 1024 * 1024) {
                    Toast.makeText(
                        this,
                        "Image size too big! [Maximum Size: 4MB]",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                try {
                    //uj kep mentese fajlba
                    val timeStamp: String = sdtf.format(Date())
                    val fileName =
                        "${latestStuff!!.name?.replace("\\s".toRegex(), "_")}_${timeStamp}"

                    try {
                        val file = File.createTempFile(fileName, ".jpg", directory)
                        file.mkdirs()
                        val out = FileOutputStream(file)
                        img.compress(Bitmap.CompressFormat.PNG, 100, out)
                        currentPhotoPath = file.absolutePath
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Hiba a kép mentésekor... ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    updateStuffImage()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            UpdatePictureDialog.REQUEST_PICK_IMAGE -> {
                val uri = data!!.data!!
                currentPhotoPath = getPath(applicationContext, uri)!!
                if(currentPhotoPath == "NOTFOUND"){
                    Toast.makeText(this, "A kép nem elérhető, kérlek próbáld újra vagy válassz másikat!", Toast.LENGTH_SHORT).show()
                } else {
                    updateStuffImage()
                }
            }
        }
    }

    private fun updateStuffImage() {
        //regi kep torlese
        if (latestStuff!!.imageURL != null) {
            val oldFile = File(latestStuff!!.imageURL!!)
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }

        //uj kep url beallitasa, megjelenitese, cucc mentese
        latestStuff!!.imageURL = currentPhotoPath
        thread {
            db.stuffDAO().update(latestStuff!!)
            runOnUiThread {
                Glide.with(this).load(currentPhotoPath).centerCrop()
                    .into(latestHolder!!.binding.img)
                latestHolder!!.binding.img.visibility = View.VISIBLE
                latestHolder!!.binding.btnAddImage.visibility = View.GONE
            }
        }
    }

    fun choosePicture(holder: StuffListAdapter.StuffViewHolder, s: Stuff){
        latestHolder = holder
        latestStuff = s

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            UpdatePictureDialog.REQUEST_PICK_IMAGE
        )
    }

    /**
     * TODO képkezelés átalakítása 3.0:
     *  startActivityForResult lecserelese korszerubb technologiara (???)
     */
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePicture(holder: StuffListAdapter.StuffViewHolder, s: Stuff) {
        latestHolder = holder
        latestStuff = s

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, UpdatePictureDialog.REQUEST_CAMERA_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForExternalStorage(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("Engedély szükséges!")
            .setMessage(R.string.permission_extstorage_rationale)
            .setPositiveButton("Engedélyezés"){_,_ ->
                request.proceed()
            }
            .setNegativeButton("Tiltás"){_,_ ->
                request.cancel()
            }
            .show()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onExtStorageDenied() {
        Toast.makeText(this, "Engedély megtagadva, képek mentése funkció nem érhető el.", Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgain() {
        Toast.makeText(this, "Az engedély a beállításokban módosítható, addig nem menthető majd kép.", Toast.LENGTH_SHORT).show()
    }

    override fun onAddressDeleted(item: MyAddress, addressListAdapter: AddressListAdapter) {
        thread{
            db.stuffDAO().deleteAddress(item.id!!)
            db.myAddressDAO().deleteItem(item)
            runOnUiThread {
                addressListAdapter.deleteItem(item)
            }
        }
    }

    override fun onCategoryDeleted(item: Category, categoryListAdapter: CategoryListAdapter) {
        thread{
            db.stuffDAO().deleteCategory(item.id!!)
            db.categoryDAO().deleteItem(item)
            runOnUiThread {
                categoryListAdapter.deleteItem(item)
            }
        }
    }

    override fun onAddressCreated(newItem: MyAddress, addressListAdapter: AddressListAdapter?) {
        thread{
            db.myAddressDAO().insert(newItem)
            runOnUiThread {
                addressListAdapter?.addItem(newItem)
            }
        }
    }

    override fun onAddressModified(newItem: MyAddress, addressListAdapter: AddressListAdapter?) {
        thread{
            db.myAddressDAO().update(newItem)
            runOnUiThread{
                addressListAdapter?.notifyItemChanged(addressListAdapter.addressList.indexOf(newItem))
            }
        }
    }

    override fun onCategoryCreated(newItem: Category, categoryListAdapter: CategoryListAdapter?) {
        thread{
            db.categoryDAO().insert(newItem)
            runOnUiThread {
                categoryListAdapter?.addItem(newItem)
            }
        }
    }

    override fun onCategoryModified(newItem: Category, categoryListAdapter: CategoryListAdapter?) {
        thread{
            db.categoryDAO().update(newItem)
            runOnUiThread{
                categoryListAdapter?.notifyItemChanged(categoryListAdapter.categoryList.indexOf(newItem))
            }
        }
    }

    fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        try {
            // Uri is different in versions after KITKAT (Android 4.4), we need to
            // deal with different Uris.
            if (needToCheckUri && DocumentsContract.isDocumentUri(
                    context.getApplicationContext(),
                    uri
                )
            ) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("image" == type) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context.contentResolver
                    .query(uri, projection, selection, selectionArgs, null) ?: throw Exception("cursor is null!")
                val columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIdx)
                }
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } catch(e: Exception){
            e.printStackTrace()
        }
        return NOTFOUND
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun clearUpImages(){
        //Lekérjük a cuccok listáját és csinálunk a neveikből egy String listát
        thread{
            val stuffList = db.stuffDAO().getAll()
            val nameList = mutableListOf<String>()
            stuffList.forEach { stuff ->
                nameList.add("${stuff.name?.replace("\\s".toRegex(), "_")}")
            }
            //Lekérjük a fájlokat, amik vannak a mappában
            val pictures = directory.listFiles()
            if(pictures == null || pictures.isEmpty()) return@thread
            //megnézzük mindegyikre hogy van-e hozzá tárolva cucc, ha nincs akkor töröljük
            //ha egy cucchoz több kép is van, akkor csak a legutolsót tartjuk meg
            val stuffImageCounter: HashMap<String, Pair<Date, File>> = HashMap()
            pictures.forEach{ picFile ->
                val stuffNameAndDate = getStuffDataFromPicture(picFile.nameWithoutExtension)
                val stuffDate = sdtf.parse(stuffNameAndDate.second)
                val stuffName = stuffNameAndDate.first
                if(stuffImageCounter.containsKey(stuffName)){
                    val storedData = stuffImageCounter[stuffName]
                    if(storedData!!.first.before(stuffDate)){
                        stuffImageCounter[stuffName] = Pair(stuffDate!!, picFile)
                    }
                } else {
                    stuffImageCounter[stuffName] = Pair(stuffDate!!, picFile)
                }
            }
            val keepImages = mutableListOf<File>()
            stuffImageCounter.keys.forEach{ stuffName ->
                if(nameList.contains(stuffName)) {
                    keepImages.add(stuffImageCounter[stuffName]!!.second)
                }
            }
            pictures.forEach{ pic ->
                if(!keepImages.contains(pic)){
                    pic.delete()
                }
            }
        }
    }

    private fun getStuffDataFromPicture(fileName: String): Pair<String, String>{
        var stuffName = fileName.substring(0, fileName.lastIndexOf('_'))
        stuffName = stuffName.substring(0, stuffName.lastIndexOf('_'))
        val stuffDate = fileName.substring(stuffName.length+1)
        return Pair(stuffName, stuffDate)
    }
}