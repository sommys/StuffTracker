package hu.bme.aut.stufftracker.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.stufftracker.adapter.CategoryListAdapter
import hu.bme.aut.stufftracker.data.StuffDatabase
import hu.bme.aut.stufftracker.databinding.ActivityCategoryManagerBinding
import hu.bme.aut.stufftracker.dialog.NewCategoryDialog
import hu.bme.aut.stufftracker.domain.Category
import kotlin.concurrent.thread

class CategoryManagerActivity : AppCompatActivity(), CategoryListAdapter.CategoryItemListener,
    NewCategoryDialog.NewCategoryDialogListener {
    private lateinit var binding: ActivityCategoryManagerBinding
    private lateinit var categoryListAdapter : CategoryListAdapter
    private lateinit var db: StuffDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryManagerBinding.inflate(layoutInflater)
        db = StuffDatabase.getDatabase(applicationContext)
        categoryListAdapter = CategoryListAdapter(this, supportFragmentManager)
        binding.categoriesRv.adapter = categoryListAdapter
        binding.categoriesRv.layoutManager = LinearLayoutManager(applicationContext)
        thread{
            val categoryList = db.categoryDAO().getAll()
            runOnUiThread{ categoryListAdapter.update(categoryList) }
        }
        binding.fabAddCategory.setOnClickListener {
            val dialog = NewCategoryDialog(null)
            dialog.show(supportFragmentManager, "NEWCATEGORY_DIALOG")
        }
        binding.fabSaveCategories.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    override fun onItemDeleted(item: Category) {
        thread{
            db.categoryDAO().deleteItem(item)
            db.stuffDAO().deleteCategory(item.id!!)
            runOnUiThread {
                categoryListAdapter.deleteItem(item)
            }
        }
    }

    override fun onCategoryCreated(newItem: Category) {
        thread{
            db.categoryDAO().insert(newItem)
            runOnUiThread {
                categoryListAdapter.addItem(newItem)
            }
        }
    }

    override fun onCategoryModified(newItem: Category) {
        thread{
            db.categoryDAO().update(newItem)
            runOnUiThread{
                categoryListAdapter.notifyItemChanged(categoryListAdapter.categoryList.indexOf(newItem))
            }
        }
    }
}