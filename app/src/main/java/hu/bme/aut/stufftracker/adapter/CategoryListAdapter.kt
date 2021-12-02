package hu.bme.aut.stufftracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.stufftracker.databinding.CategoryItemBinding
import hu.bme.aut.stufftracker.dialog.NewCategoryDialog
import hu.bme.aut.stufftracker.domain.Category

class CategoryListAdapter(private val listener: CategoryItemListener, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder>() {
    var categoryList: ArrayList<Category> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CategoryViewHolder(
        CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        var cat = categoryList[position]
        holder.binding.nameTv.text = cat.name?.uppercase() ?: ""
        holder.binding.btnEditCategory.setOnClickListener {
            var dialog = NewCategoryDialog(cat)
            dialog.show(fragmentManager, "NEWCATEGORY_DIALOG")
        }
        holder.binding.btnDeleteCategory.setOnClickListener {
            var confirmDeleteDialog = AlertDialog.Builder(listener as Context)
            confirmDeleteDialog.setTitle("A kategória törlésével a benne lévő dolgok is törlődnek. Biztosan törli?");
            confirmDeleteDialog.setPositiveButton("Törlés"){_,_->
                listener.onItemDeleted(cat)
            }
            confirmDeleteDialog.setNegativeButton("Mégsem", null)
            confirmDeleteDialog.create().show()
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun addItem(item: Category) {
        categoryList.add(item)
        notifyItemInserted(categoryList.size - 1)
    }

    fun update(categories: List<Category>) {
        categoryList.clear()
        categoryList.addAll(categories)
        notifyDataSetChanged()
    }

    fun deleteItem(item: Category){
        var pos = categoryList.indexOf(item)
        categoryList.remove(item)
        notifyItemRemoved(pos)
    }

    interface CategoryItemListener{
        fun onItemDeleted(item: Category)
    }

    inner class CategoryViewHolder(val binding: CategoryItemBinding) :RecyclerView.ViewHolder(binding.root)
}