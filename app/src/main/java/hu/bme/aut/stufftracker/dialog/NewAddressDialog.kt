package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import hu.bme.aut.stufftracker.databinding.NewAddressDialogBinding
import hu.bme.aut.stufftracker.domain.MyAddress
import java.lang.Integer.parseInt
import java.util.*

class NewAddressDialog(var existingAddress: MyAddress?): DialogFragment() {
    private lateinit var listener: NewAddressDialogListener
    private lateinit var binding: NewAddressDialogBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewAddressDialogListener
            ?: throw RuntimeException("Activity must implement the NewAddressDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        var title: String = if(existingAddress!=null) "Cím Szerkesztése" else "Új Cím Hozzáadása"
        builder.setTitle(title)
        binding = NewAddressDialogBinding.inflate(layoutInflater)
        if(existingAddress != null){
            binding.zipEt.setText(existingAddress!!.zipCode)
            binding.cityEt.setText(existingAddress!!.city)
            binding.countryEt.setText(existingAddress!!.country)
            binding.streetEt.setText(existingAddress!!.street)
            binding.streetNumEt.setText(existingAddress!!.streetNum.toString())
            binding.aliasEt.setText(existingAddress!!.alias)
        }
        builder.setPositiveButton("MENTÉS") {_,_ ->
            if(isValid()){
                if(existingAddress == null) {
                    listener.onAddressCreated(getAddress())
                } else {
                    listener.onAddressModified(modifyAddress())
                }
            } else {
                Toast.makeText(requireContext(), "Kérlek töltsd ki helyesen a cím adatokat!", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("MÉGSE", null)
        builder.setView(binding.root)
        return builder.create()
    }

    private fun isValid():Boolean = (
        binding.zipEt.text.isNotEmpty() &&
        binding.cityEt.text.isNotEmpty() &&
        binding.countryEt.text.isNotEmpty() &&
        binding.streetEt.text.isNotEmpty() &&
        binding.streetNumEt.text.isDigitsOnly() && binding.streetNumEt.text.isNotEmpty() &&
        binding.aliasEt.text.isNotEmpty())

    private fun getAddress() = MyAddress(
        zipCode = binding.zipEt.text.toString(),
        city = binding.cityEt.text.toString(),
        country = binding.countryEt.text.toString(),
        street = binding.streetEt.text.toString(),
        streetNum = parseInt(binding.streetNumEt.text.toString()),
        alias = binding.aliasEt.text.toString()
    )

    private fun modifyAddress(): MyAddress {
        existingAddress!!.zipCode = binding.zipEt.text.toString()
        existingAddress!!.city = binding.cityEt.text.toString()
        existingAddress!!.country = binding.countryEt.text.toString()
        existingAddress!!.street = binding.streetEt.text.toString()
        existingAddress!!.streetNum = parseInt(binding.streetNumEt.text.toString())
        existingAddress!!.alias = binding.aliasEt.text.toString()
        return existingAddress!!
    }

    interface NewAddressDialogListener{
        fun onAddressCreated(newItem: MyAddress)
        fun onAddressModified(newItem: MyAddress)
    }
}