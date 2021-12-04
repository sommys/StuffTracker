package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.adapter.CategorySpinnerAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.NewStuffDialogBinding
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import java.lang.Integer.parseInt
import kotlin.concurrent.thread

class NewStuffDialog(var address: MyAddress, var existingStuff: Stuff?, var listener: NewStuffDialogListener): DialogFragment() {
    private lateinit var binding: NewStuffDialogBinding
    private lateinit var db: StuffDatabase

    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = StuffDatabase.getDatabase(context)
    }

    private fun isValid() =
        binding.nameEt.text.isNotEmpty() &&
        binding.quantityEt.text.isDigitsOnly() && binding.quantityEt.text.isNotEmpty() &&
        binding.descEt.text.isNotEmpty()

    private fun getStuff() = Stuff(
        name = binding.nameEt.text.toString(),
        quantity = parseInt(binding.quantityEt.text.toString()),
        description = binding.descEt.text.toString(),
        imageURL = null,
        addressId = address.id,
        categoryId = (binding.catSpinner.selectedItem as Category).id
    )

    private fun modifyStuff(): Stuff{
        existingStuff!!.name = binding.nameEt.text.toString()
        existingStuff!!.quantity = parseInt(binding.quantityEt.text.toString())
        existingStuff!!.description = binding.descEt.text.toString()
        existingStuff!!.addressId = address.id
        existingStuff!!.categoryId = (binding.catSpinner.selectedItem as Category).id
        return existingStuff!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        var title: String = if(existingStuff!=null) "Dolog Szerkesztése" else "Új Dolog Hozzáadása"
        builder.setTitle(title)
        binding = NewStuffDialogBinding.inflate(layoutInflater)
        builder.setPositiveButton("MENTÉS"){ _,_->
            if(isValid()){
                if(existingStuff == null) {
                    listener.onStuffCreated(getStuff())
                } else {
                    listener.onStuffModified(modifyStuff())
                }
            } else {
                Toast.makeText(requireContext(), "Kérlek töltsd ki helyesen a dolog adatait!", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("MÉGSE", null)
        if(existingStuff != null){
            binding.nameEt.setText(existingStuff!!.name)
            binding.quantityEt.setText("${existingStuff!!.quantity!!}")
            binding.descEt.setText(existingStuff!!.description)
        }
        thread{
            var catList = db.categoryDAO().getAll()
            requireActivity().runOnUiThread {
                binding.catSpinner.adapter = CategorySpinnerAdapter(requireContext(), R.layout.category_spinner, catList as MutableList<Category>)
                if(existingStuff != null){
                    thread {
                        var cat = db.categoryDAO().getById(existingStuff!!.categoryId!!)
                        requireActivity().runOnUiThread {
                            for (i in 0 until binding.catSpinner.adapter.count) {
                                if (binding.catSpinner.getItemAtPosition(i).equals(cat)) {
                                    binding.catSpinner.setSelection(i)
                                    break
                                }
                            }
                        }
                    }
                }
            }
            db.close()
        }
        builder.setView(binding.root)
        return builder.create()
    }

    interface NewStuffDialogListener{
        fun onStuffCreated(newItem: Stuff)
        fun onStuffModified(newItem: Stuff)
    }
}