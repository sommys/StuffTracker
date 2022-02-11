package hu.bme.aut.stufftracker.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import hu.bme.aut.stufftracker.fragment.AddressFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(),
    AddressListAdapter.AddressItemListener,
    CategoryListAdapter.CategoryItemListener,
    NewAddressDialog.NewAddressDialogListener,
    NewCategoryDialog.NewCategoryDialogListener{
    companion object{
        private const val REQUEST_CAMERA_IMAGE = 101
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var addressPagerAdapter: AddressPagerAdapter
    private lateinit var navController: NavController
    private lateinit var db : StuffDatabase
    private var latestHolder: StuffListAdapter.StuffViewHolder? = null
    private var latestStuff: Stuff? = null
    private lateinit var currentPhotoPath: String
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
        if(requestCode == REQUEST_CAMERA_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val img = data!!.extras!!["data"] as Bitmap
                Log.d("IMG_BYTECOUNT", img.allocationByteCount.toString())
                if (img.allocationByteCount > 4 * 1024 * 1024) {
                    Toast.makeText(this, "Image size too big! [Maximum Size: 4MB]", Toast.LENGTH_SHORT).show()
                    return
                }
                try {
                    //uj kep mentese fajlba
                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val fileName = "${latestStuff!!.name?.replace("\\s".toRegex(),"_")}_${timeStamp}"

                    try{
                        val path = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "stuffTracker")
                        val file = File.createTempFile(fileName, ".jpg", path)
                        file.mkdirs()
                        val out = FileOutputStream(file)
                        img.compress(Bitmap.CompressFormat.PNG, 100, out)
                        currentPhotoPath = file.absolutePath
                    } catch(e : Exception){
                        Toast.makeText(this, "Hiba a kép mentésekor... ${e.message}", Toast.LENGTH_LONG).show()
                    }

                    //regi kep torlese
                    if(latestStuff!!.imageURL != null){
                        val oldFile = File(latestStuff!!.imageURL!!)
                        if(oldFile.exists()) { oldFile.delete() }
                    }

                    //uj kep url beallitasa, megjelenitese, cucc mentese
                    latestStuff!!.imageURL = currentPhotoPath
                    thread{
                        db.stuffDAO().update(latestStuff!!)
                        runOnUiThread{
                            Glide.with(this).load(currentPhotoPath).centerCrop().into(latestHolder!!.binding.img)
                            latestHolder!!.binding.img.visibility = View.VISIBLE
                            latestHolder!!.binding.btnAddImage.visibility = View.GONE
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            }
        }
    }

    /**
     * TODO képkezelés átalakítása 2.0:
     *  1. engedelykezeles bevezetese
     *  2. meglevo kep valasztas lehetosege
     *  3. startActivityForResult lecserelese korszerubb technologiara (???)
     */
    fun takePicture(holder: StuffListAdapter.StuffViewHolder, s: Stuff) {
        latestHolder = holder
        latestStuff = s

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA_IMAGE)
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
}