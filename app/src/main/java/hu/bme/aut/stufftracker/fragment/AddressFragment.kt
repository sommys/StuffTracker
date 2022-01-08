package hu.bme.aut.stufftracker.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.stufftracker.R
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.adapter.CategorySpinnerAdapter
import hu.bme.aut.stufftracker.adapter.StuffListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.FragmentAddressBinding
import hu.bme.aut.stufftracker.dialog.NewStuffDialog
import hu.bme.aut.stufftracker.domain.Category
import hu.bme.aut.stufftracker.domain.MyAddress
import hu.bme.aut.stufftracker.domain.Stuff
import kotlin.concurrent.thread

class AddressFragment(var address : MyAddress, var mContext: Context): Fragment(), NewStuffDialog.NewStuffDialogListener, StuffListAdapter.StuffItemListener{
    private lateinit var binding: FragmentAddressBinding
    private var categories:ArrayList<Category> = ArrayList()
    private var db = StuffDatabase.getDatabase(mContext)
    private lateinit var c: Category
    private lateinit var spinnerAdapter: CategorySpinnerAdapter
    lateinit var stuffListAdapter: StuffListAdapter
    var refreshNeeded: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        binding.addressTv.text = address.toString()
        stuffListAdapter = StuffListAdapter(address, this, requireActivity().supportFragmentManager, requireActivity() as MainActivity)
        binding.rv.adapter = stuffListAdapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())

        binding.fabAddStuff.setOnClickListener {
            var dialog = NewStuffDialog(address, null, this)
            dialog.show(parentFragmentManager, "NEWSTUFF_DIALOG")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        categories.clear()
        categories.add(Category("MINDEN"))
        thread {
            var catList = db.categoryDAO().getAll()
            categories.addAll(catList)
            requireActivity().runOnUiThread {
                spinnerAdapter =
                    CategorySpinnerAdapter(mContext, R.layout.category_spinner, categories)
                binding.categorySpinner.adapter = spinnerAdapter
                binding.categorySpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            c = spinnerAdapter.getItem(position)
                            thread {
                                if (c.name == "MINDEN") {
                                    var stuffList = db.stuffDAO()
                                        .getStuffWithAddress(address.id!!)
                                    requireActivity().runOnUiThread {
                                        stuffListAdapter.update(stuffList)
                                    }
                                } else {
                                    var stuffList = db.stuffDAO()
                                        .getStuffWithAddressAndCategory(address.id!!, c.id!!)
                                    requireActivity().runOnUiThread {
                                        stuffListAdapter.update(stuffList)
                                    }
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
        }
    }

    override fun onStuffCreated(newItem: Stuff) {
        stuffListAdapter.addItem(newItem)
        thread{
            db.stuffDAO().insert(newItem)
        }
    }

    override fun onStuffModified(newItem: Stuff){
        thread{
            db.stuffDAO().update(newItem)
            if(c.name == "MINDEN"){
                var stuffList = db.stuffDAO()
                    .getStuffWithAddress(address.id!!)
                requireActivity().runOnUiThread {
                    stuffListAdapter.update(stuffList)
                }
            } else {
                var stuffList = db.stuffDAO()
                    .getStuffWithAddressAndCategory(address.id!!, c.id!!)
                requireActivity().runOnUiThread {
                    stuffListAdapter.update(stuffList)
                }
            }
        }
    }

    override fun onItemDeleted(item: Stuff) {
        thread{
            db.stuffDAO().deleteItem(item)
            requireActivity().runOnUiThread {
                stuffListAdapter.deleteItem(item)
                //todo kep kitorlese tarhelyrol
            }
        }
    }

    override fun onItemModified(item: Stuff) {
        onStuffModified(item)
    }

    override fun setUserVisibleHint(visible: Boolean) {
        super.setUserVisibleHint(visible)
        if (visible && isResumed) {
            onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!refreshNeeded) {
            return
        }

        refreshNeeded = false
        c = binding.categorySpinner.selectedItem as Category
        thread {
            if(c.name == "MINDEN"){
                var stuffList = db.stuffDAO()
                    .getStuffWithAddress(address.id!!)
                requireActivity().runOnUiThread {
                    stuffListAdapter.update(stuffList)
                }
            } else {
                var stuffList = db.stuffDAO().getStuffWithAddressAndCategory(address.id!!, c.id!!)
                requireActivity().runOnUiThread {
                    stuffListAdapter.update(stuffList)
                }
            }
        }
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    override fun onStuffAddressModified(item: Stuff) {
        (mContext as MainActivity).refreshFragment(item.addressId!!)
    }

    fun deleteStuff(item: Stuff){
        stuffListAdapter.deleteItem(item)
    }
}