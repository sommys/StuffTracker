package hu.bme.aut.stufftracker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress

class AddressSpinnerAdapter(var mcontext: Context, var resource: Int, var addresses: MutableList<MyAddress>) :
    ArrayAdapter<MyAddress>(mcontext, resource, addresses) {

    override fun getCount() = addresses.size

    override fun getItem(position: Int) = addresses[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var label: TextView = super.getView(position, convertView, parent) as TextView
        label.text = addresses[position].alias
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var label: TextView = super.getDropDownView(position, convertView, parent) as TextView
        label.text = addresses[position].toString()
        return label
    }
}
