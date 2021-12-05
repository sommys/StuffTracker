package hu.bme.aut.stufftracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.stufftracker.adapter.AddressListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.ActivityAddressManagerBinding
import hu.bme.aut.stufftracker.dialog.NewAddressDialog
import hu.bme.aut.stufftracker.domain.MyAddress
import kotlin.concurrent.thread

class AddressManagerActivity : AppCompatActivity(), AddressListAdapter.AddressItemListener, NewAddressDialog.NewAddressDialogListener {
    private lateinit var binding: ActivityAddressManagerBinding
    private lateinit var addressListAdapter : AddressListAdapter
    private lateinit var db: StuffDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressManagerBinding.inflate(layoutInflater)
        db = StuffDatabase.getDatabase(applicationContext)
        addressListAdapter = AddressListAdapter(this, supportFragmentManager)
        binding.addressRv.adapter = addressListAdapter
        binding.addressRv.layoutManager = LinearLayoutManager(applicationContext)
        thread{
            val addressList = db.myAddressDAO().getAll()
            runOnUiThread{
                addressListAdapter.update(addressList)
            }
        }
        binding.fabAddAddress.setOnClickListener {
            val dialog = NewAddressDialog(null)
            dialog.show(supportFragmentManager, "NEWADDRESS_DIALOG")
        }
        binding.fabSaveAddresses.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    override fun onItemDeleted(item: MyAddress) {
        thread{
            db.stuffDAO().deleteAddress(item.id!!)
            db.myAddressDAO().deleteItem(item)
            runOnUiThread {
                addressListAdapter.deleteItem(item)
            }
        }
    }

    override fun onAddressCreated(newItem: MyAddress) {
        thread{
            db.myAddressDAO().insert(newItem)
            runOnUiThread {
                addressListAdapter.addItem(newItem)
            }
        }
    }

    override fun onAddressModified(newItem: MyAddress) {
        thread{
            db.myAddressDAO().update(newItem)
            runOnUiThread{
                addressListAdapter.notifyItemChanged(addressListAdapter.addressList.indexOf(newItem))
            }
        }
    }
}