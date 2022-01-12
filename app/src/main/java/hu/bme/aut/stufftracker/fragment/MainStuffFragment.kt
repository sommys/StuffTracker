package hu.bme.aut.stufftracker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.adapter.AddressPagerAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.FragmentMainStuffBinding
import kotlin.concurrent.thread

class MainStuffFragment: Fragment() {
    private lateinit var binding: FragmentMainStuffBinding
    private lateinit var db : StuffDatabase
    private var addressPagerAdapter: AddressPagerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainStuffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        addressPagerAdapter = AddressPagerAdapter(childFragmentManager)
        db = StuffDatabase.getDatabase(requireContext())
        thread{
            val addrList = db.myAddressDAO().getAll()
            if(addrList.isEmpty()){
                binding.addressPager.visibility = View.GONE
                binding.emptyAddressListTv.visibility = View.VISIBLE
            } else {
                binding.emptyAddressListTv.visibility = View.GONE
                binding.addressPager.visibility = View.VISIBLE
                for(a in addrList){
                    var f = AddressFragment(a, requireContext(), requireActivity() as MainActivity)
                    addressPagerAdapter!!.addFragment(f)
                }
                requireActivity().runOnUiThread {
                    binding.addressPager.adapter = addressPagerAdapter
                }
            }
        }
    }
}