package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.adapter.AddressSpinnerAdapter
import hu.bme.aut.stufftracker.adapter.CategorySpinnerAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.ChangeAddressDialogBinding
import hu.bme.aut.stufftracker.databinding.NewAddressDialogBinding
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import kotlin.concurrent.thread

class ChangeAddressDialog(var existingAddress: MyAddress, var stuff: Stuff, var listener: ChangeAddressDialogListener): DialogFragment() {
    private lateinit var binding: ChangeAddressDialogBinding
    private lateinit var db: StuffDatabase
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        db = StuffDatabase.getDatabase(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Dolog áthelyezése")
        binding = ChangeAddressDialogBinding.inflate(layoutInflater)
        binding.stuffAddress.text = existingAddress.toString()
        binding.stuffNameTv.text = stuff.name
        if(stuff.imageURL != null){
            Glide.with(this).load(stuff.imageURL).into(binding.imgStuff)
        }
        builder.setPositiveButton("ÁTHELYEZÉS") {_,_ ->
            thread{
                stuff.addressId = (binding.addressSpinner.selectedItem as MyAddress).id!!
                db.stuffDAO().update(stuff)
                (mContext as MainActivity).runOnUiThread{
                    listener.onAddressChanged(stuff)
                }
                db.close()
            }
        }
        builder.setNegativeButton("MÉGSE", null)
        thread{
            var addrList = db.myAddressDAO().getOthers(existingAddress.id!!)
            requireActivity().runOnUiThread {
                if(addrList.isEmpty()){
                    binding.addressSpinner.visibility = View.GONE
                    binding.noAddressTv.visibility = View.VISIBLE
                } else {
                    binding.addressSpinner.adapter = AddressSpinnerAdapter(requireContext(), R.layout.address_spinner, addrList as MutableList<MyAddress>)
                    binding.addressSpinner.visibility = View.VISIBLE
                    binding.noAddressTv.visibility = View.GONE
                }
            }
            db.close()
        }
        builder.setView(binding.root)
        return builder.create()
    }


    interface ChangeAddressDialogListener{
        fun onAddressChanged(item: Stuff)
    }
}