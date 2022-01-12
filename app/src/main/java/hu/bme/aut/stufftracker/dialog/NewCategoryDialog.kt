package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import hu.bme.aut.stufftracker.adapter.CategoryListAdapter
import hu.bme.aut.stufftracker.databinding.NewCategoryDialogBinding
import hu.bme.aut.stufftracker.domain.Category

class NewCategoryDialog(var existingCategory: Category?, private var categoryListAdapter: CategoryListAdapter): DialogFragment() {
    private lateinit var listener: NewCategoryDialogListener
    private lateinit var binding: NewCategoryDialogBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NewCategoryDialogListener
            ?: throw RuntimeException("Activity must implement the NewCategoryDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        var title: String = if(existingCategory!=null) "Kategória Szerkesztése" else "Új Kategória Hozzáadása"
        builder.setTitle(title)
        binding = NewCategoryDialogBinding.inflate(layoutInflater)
        if(existingCategory != null){
            binding.nameEt.setText(existingCategory!!.name.toString())
        }
        builder.setPositiveButton("MENTÉS") {_,_ ->
            if(isValid()){
                if(existingCategory == null) {
                    listener.onCategoryCreated(getCategory(), categoryListAdapter)
                } else {
                    listener.onCategoryModified(modifyCategory(), categoryListAdapter)
                }
            } else {
                Toast.makeText(requireContext(), "Kérlek írj be egy érvényes kategórianevet!", Toast.LENGTH_LONG).show()
            }
        }
        builder.setNegativeButton("MÉGSE", null)
        builder.setView(binding.root)
        return builder.create()
    }

    private fun isValid() = binding.nameEt.text.isNotEmpty() && binding.nameEt.text.toString().uppercase() != "MINDEN"

    private fun getCategory() = Category(
        name = binding.nameEt.text.toString()
    )

    private fun modifyCategory(): Category {
        existingCategory!!.name = binding.nameEt.text.toString()
        return existingCategory!!
    }

    interface NewCategoryDialogListener{
        fun onCategoryCreated(newItem: Category, categoryListAdapter: CategoryListAdapter?)
        fun onCategoryModified(newItem: Category, categoryListAdapter: CategoryListAdapter?)
    }
}