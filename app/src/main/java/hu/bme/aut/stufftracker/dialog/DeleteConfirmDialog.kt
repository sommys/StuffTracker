package hu.bme.aut.stufftracker.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import hu.bme.aut.stufftracker.adapter.StuffListAdapter
import hu.bme.aut.stufftracker.databinding.StuffItemBinding
import hu.bme.aut.stufftracker.domain.Stuff
import java.io.File

class DeleteConfirmDialog(private val s: Stuff, private val listener: StuffListAdapter.StuffItemListener) : DialogFragment() {

    private lateinit var binding: StuffItemBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Biztosan törölni akarod az alábbi tárgyat?")
        binding = StuffItemBinding.inflate(layoutInflater)
        binding.nameTv.text = s.name
        binding.descTv.text = s.description
        binding.quantityTv.text = "${s.quantity} db"
        binding.btnAddImage.visibility = View.GONE
        binding.img.visibility = View.GONE
        if(s.imageURL != null){
            binding.img.visibility = View.VISIBLE
            Glide.with(requireActivity()).load(File(s.imageURL!!)).centerCrop().into(binding.img)
        }
        binding.btnChangeAddress.visibility = View.GONE
        binding.btnDeleteStuff.visibility = View.GONE
        binding.btnEditStuff.visibility = View.GONE
        builder.setPositiveButton("TÖRLÉS"){ _,_->
            listener.onItemDeleted(s)
        }
        builder.setNegativeButton("MÉGSE", null)
        builder.setView(binding.root)
        return builder.create()
    }
}