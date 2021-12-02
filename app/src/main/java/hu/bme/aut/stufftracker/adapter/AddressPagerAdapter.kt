package hu.bme.aut.stufftracker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hu.bme.aut.stufftracker.domain.Stuff
import hu.bme.aut.stufftracker.fragment.AddressFragment

class AddressPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var addressFragments: ArrayList<AddressFragment> = ArrayList()
    override fun getItem(position: Int): Fragment = addressFragments[position]

    override fun getCount() : Int = addressFragments.size

    public fun addFragment(f : AddressFragment){
        addressFragments.add(f)
        notifyDataSetChanged()
    }

    fun deleteItem(item: Stuff) {
        for(f: AddressFragment in addressFragments){
            f.deleteStuff(item)
        }
    }
}