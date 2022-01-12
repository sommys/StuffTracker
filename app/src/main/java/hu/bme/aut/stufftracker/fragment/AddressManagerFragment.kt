package hu.bme.aut.stufftracker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.adapter.AddressListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.FragmentAddressManagerBinding
import hu.bme.aut.stufftracker.dialog.NewAddressDialog
import kotlin.concurrent.thread

class AddressManagerFragment : Fragment() {
    private lateinit var binding: FragmentAddressManagerBinding
    private lateinit var navController: NavController
    private lateinit var db : StuffDatabase
    private lateinit var addressListAdapter : AddressListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val action = AddressManagerFragmentDirections.addressManagerToMainStuff()
                navController.navigate(action)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddressManagerBinding.inflate(layoutInflater)
        addressListAdapter = AddressListAdapter(requireActivity() as MainActivity, parentFragmentManager)
        binding.addressRv.adapter = addressListAdapter
        binding.addressRv.layoutManager = LinearLayoutManager(requireContext())
        db = StuffDatabase.getDatabase(requireContext())
        thread{
            val addressList = db.myAddressDAO().getAll()
            requireActivity().runOnUiThread{
                addressListAdapter.update(addressList)
            }
        }
        binding.fabAddAddress.setOnClickListener {
            val dialog = NewAddressDialog(null, addressListAdapter)
            dialog.show(parentFragmentManager, "NEWADDRESS_DIALOG")
        }
        binding.fabSaveAddresses.setOnClickListener {
            val action = AddressManagerFragmentDirections.addressManagerToMainStuff()
            navController.navigate(action)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }
}