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
import com.bumptech.glide.Glide
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.adapter.AddressPagerAdapter
import hu.bme.aut.stufftracker.adapter.StuffListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.ActivityMainBinding
import hu.bme.aut.stufftracker.dialog.ChangePINDialog
import hu.bme.aut.stufftracker.domain.Stuff
import hu.bme.aut.stufftracker.fragment.AddressFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
//TODO all around DB kezeles atgondolasa + javitasa
class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var addressPagerAdapter: AddressPagerAdapter
    private lateinit var db : StuffDatabase
    private val REQUEST_IMAGE_CAPTURE = 1
    private var latestHolder: StuffListAdapter.StuffViewHolder? = null
    private var latestStuff: Stuff? = null
    private lateinit var currentPhotoPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        db = StuffDatabase.getDatabase(this)

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        addressPagerAdapter = AddressPagerAdapter(supportFragmentManager)
        thread{
            val addrList = db.myAddressDAO().getAll()
            for(a in addrList){
                addressPagerAdapter.addFragment(AddressFragment(a, this))
            }
            runOnUiThread {
                binding.addressPager.adapter = addressPagerAdapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.manageAddressMenuItem -> {
                val intent = Intent(this, AddressManagerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
            }
            R.id.manageCategoryMenuItem -> {
                val intent = Intent(this, CategoryManagerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
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

    //todo kepmeret csokkentese
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
}