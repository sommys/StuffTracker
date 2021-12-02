package hu.bme.aut.stufftracker.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.stufftracker.databinding.AddressItemBinding
import hu.bme.aut.stufftracker.dialog.NewAddressDialog
import hu.bme.aut.stufftracker.domain.MyAddress

class AddressListAdapter(private val listener: AddressItemListener, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<AddressListAdapter.AddressViewHolder>() {
    var addressList: ArrayList<MyAddress> = ArrayList<MyAddress>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AddressViewHolder(
        AddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        var adr = addressList[position]
        holder.binding.addressTv.text = "${adr.zipCode} ${adr.city}, ${adr.country}, ${adr.street} ${adr.streetNum}"
        holder.binding.aliasTv.text = adr.alias
        holder.binding.btnEditAddress.setOnClickListener {
            var dialog = NewAddressDialog(adr)
            dialog.show(fragmentManager, "NEWADDRESS_DIALOG")
        }
        holder.binding.btnDeleteAddress.setOnClickListener {
            var confirmDeleteDialog = AlertDialog.Builder(listener as Context)
            confirmDeleteDialog.setTitle("A lakcím törlésével a benne lévő dolgok is törlődnek. Biztosan törli?");
            confirmDeleteDialog.setPositiveButton("Törlés"){_,_->
                listener.onItemDeleted(adr)
            }
            confirmDeleteDialog.setNegativeButton("Mégsem", null)
            confirmDeleteDialog.create().show()
        }
    }

    override fun getItemCount(): Int = addressList.size

    fun addItem(item: MyAddress) {
        addressList.add(item)
        notifyItemInserted(addressList.size - 1)
    }

    fun update(addresss: List<MyAddress>) {
        addressList.clear()
        addressList.addAll(addresss)
        notifyDataSetChanged()
    }

    fun deleteItem(item: MyAddress){
        var pos = addressList.indexOf(item)
        addressList.remove(item)
        notifyItemRemoved(pos)
    }

    interface AddressItemListener{
        fun onItemDeleted(item: MyAddress)
    }

    inner class AddressViewHolder(val binding: AddressItemBinding) :RecyclerView.ViewHolder(binding.root)
}