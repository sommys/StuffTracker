package hu.bme.aut.stufftracker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import hu.bme.aut.stufftracker.domain.Category

class CategorySpinnerAdapter(var mcontext: Context, var resource: Int, var categories: MutableList<Category>) :
    ArrayAdapter<Category>(mcontext, resource, categories) {

    override fun getCount() = categories.size

    override fun getItem(position: Int) = categories[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var label: TextView = super.getView(position, convertView, parent) as TextView
        label.text = categories[position].name?.uppercase()
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var label: TextView = super.getDropDownView(position, convertView, parent) as TextView
        label.text = categories[position].name?.uppercase()
        return label
    }

}