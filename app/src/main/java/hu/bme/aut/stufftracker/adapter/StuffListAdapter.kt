package hu.bme.aut.stufftracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.databinding.StuffItemBinding
import hu.bme.aut.stufftracker.dialog.ChangeAddressDialog
import hu.bme.aut.stufftracker.dialog.DeleteConfirmDialog
import hu.bme.aut.stufftracker.dialog.NewStuffDialog
import hu.bme.aut.stufftracker.dialog.UpdatePictureDialog
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import java.io.File


class StuffListAdapter(private val address: MyAddress, private val listener: StuffItemListener, private val fragmentManager: FragmentManager, private val activity: MainActivity) : RecyclerView.Adapter<StuffListAdapter.StuffViewHolder>(), ChangeAddressDialog.ChangeAddressDialogListener {
    private var stuffList: ArrayList<Stuff> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StuffViewHolder(
        StuffItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: StuffViewHolder, position: Int) {
        val s : Stuff = stuffList[position]
        holder.binding.nameTv.text = s.name
        holder.binding.descTv.text = s.description
        holder.binding.quantityTv.text = "${s.quantity} db"
        if(s.imageURL != null){
            holder.binding.btnAddImage.visibility = View.GONE
            holder.binding.img.visibility = View.VISIBLE
            Glide.with(activity).load(File(s.imageURL!!)).centerCrop().into(holder.binding.img)
        }
        holder.binding.btnEditStuff.setOnClickListener {
            val dialog = NewStuffDialog(address, s, listener as NewStuffDialog.NewStuffDialogListener)
            dialog.show(fragmentManager, "NEWSTUFF_DIALOG")
        }
        holder.binding.btnDeleteStuff.setOnClickListener {
            val dialog = DeleteConfirmDialog(s, listener)
            dialog.show(fragmentManager, "DELETECONFIRM_DIALOG")
        }
        holder.binding.btnAddImage.setOnClickListener {
            UpdatePictureDialog(activity, holder, s).show(activity.supportFragmentManager, "UPDATE_PICTURE_DIALOG")
        }
        holder.binding.img.setOnClickListener {
            UpdatePictureDialog(activity, holder, s).show(activity.supportFragmentManager, "UPDATE_PICTURE_DIALOG")
        }
        holder.binding.btnChangeAddress.setOnClickListener {
            val dialog = ChangeAddressDialog(address, s, this)
            dialog.show(fragmentManager, "CHANGEADDRESS_DIALOG")
        }
    }

    override fun getItemCount(): Int = stuffList.size


    fun addItem(item: Stuff) {
        stuffList.add(item)
        notifyItemInserted(stuffList.size - 1)
    }

    fun update(stuffs: List<Stuff>) {
        stuffList.clear()
        stuffList.addAll(stuffs)
        notifyDataSetChanged()
    }

    fun deleteItem(item: Stuff){
        if(item.imageURL != null){
            val oldFile = File(item.imageURL!!)
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }
        val pos = stuffList.indexOf(item)
        if(pos != -1) {
            stuffList.remove(item)
            notifyItemRemoved(pos)
        }
    }

    interface StuffItemListener{
        fun onItemDeleted(item: Stuff)
        fun onItemModified(item: Stuff)
        fun onStuffAddressModified(item: Stuff)
    }

    inner class StuffViewHolder(val binding: StuffItemBinding) :RecyclerView.ViewHolder(binding.root)

    override fun onAddressChanged(item: Stuff) {
        if(item.addressId != address.id){
            deleteItem(item)
        }
        listener.onStuffAddressModified(item)
    }
}