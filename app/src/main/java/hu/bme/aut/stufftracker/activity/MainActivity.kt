package hu.bme.aut.stufftracker.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * TODO all around DB kezeles atgondolasa + javitasa
 * Csak a main activity fogja nyitni és zárni a db-t, a fragmentek megkapják paraméterként
 * NavGraph-al fogjuk megoldani a fregmensek kezelését, a Main Activityben csak egy fragment tartó lesz
 * Alapból a mostani Main Activity lesz benne, ami gyakorlatilag csak egy ViewPager
 * Maradnak a settings menüben a menedzselések, az ottani menüpontok fogják a fragment tranzakciókat kezelni
 * PIN változtatás marad dialógus
 * Address és Category management is átkerül egy sima fragmentbe
 */
class MainActivity : AppCompatActivity(),
    AddressListAdapter.AddressItemListener,
    CategoryListAdapter.CategoryItemListener,
    NewAddressDialog.NewAddressDialogListener,
    NewCategoryDialog.NewCategoryDialogListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var addressPagerAdapter: AddressPagerAdapter
    private lateinit var navController: NavController
    private lateinit var db : StuffDatabase
    private val REQUEST_IMAGE_CAPTURE = 1
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

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(latestStuff!!.imageURL != null){
                val oldFile = File(latestStuff!!.imageURL!!)
                if(oldFile.exists()) { oldFile.delete() }
            }
            latestStuff!!.imageURL = currentPhotoPath
            thread{
                db.stuffDAO().update(latestStuff!!)
                runOnUiThread{
                    Glide.with(this).load(currentPhotoPath).centerCrop().into(latestHolder!!.binding.img)
                    latestHolder!!.binding.img.visibility = View.VISIBLE
                    latestHolder!!.binding.btnAddImage.visibility = View.GONE
                }
            }
        }
    }

    /**
     * TODO képkezelés átalakítása
     * 1. kepmeret csokkentese
     * 2. kep mashova mentese (konnyebben elerheto helyre)
     * 3. jobb kep nevek (stuff.name+timestamp.jpg?)
     * 4. startActivityForResult lecserelese korszerubb technologiara (???)
     */
    fun takePicture(holder: StuffListAdapter.StuffViewHolder, s: Stuff) {
        latestHolder = holder
        latestStuff = s
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        try {
            val photoFile: File? = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
                .apply { currentPhotoPath = absolutePath }
            photoFile?.also{
                val photoURI: Uri = FileProvider.getUriForFile(this, applicationContext.packageName+".provider", photoFile)
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        } catch(e: IOException){
            e.printStackTrace()
        }
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