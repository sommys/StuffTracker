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
import hu.bme.aut.stufftracker.adapter.CategoryListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.FragmentCategoryManagerBinding
import hu.bme.aut.stufftracker.dialog.NewCategoryDialog
import kotlin.concurrent.thread

class CategoryManagerFragment : Fragment(){
    private lateinit var binding: FragmentCategoryManagerBinding
    private lateinit var navController: NavController
    private lateinit var db : StuffDatabase
    private lateinit var categoryListAdapter : CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val action = CategoryManagerFragmentDirections.categoryManagerToMainStuff()
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
        binding = FragmentCategoryManagerBinding.inflate(layoutInflater)
        categoryListAdapter = CategoryListAdapter(requireActivity() as MainActivity, parentFragmentManager)
        binding.categoriesRv.adapter = categoryListAdapter
        binding.categoriesRv.layoutManager = LinearLayoutManager(requireContext())
        db = StuffDatabase.getDatabase(requireContext())
        thread{
            val categoryList = db.categoryDAO().getAll()
            requireActivity().runOnUiThread{ categoryListAdapter.update(categoryList) }
        }
        binding.fabAddCategory.setOnClickListener {
            val dialog = NewCategoryDialog(null, categoryListAdapter)
            dialog.show(parentFragmentManager, "NEWCATEGORY_DIALOG")
        }
        binding.fabSaveCategories.setOnClickListener {
            val action = CategoryManagerFragmentDirections.categoryManagerToMainStuff()
            navController.navigate(action)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }
}