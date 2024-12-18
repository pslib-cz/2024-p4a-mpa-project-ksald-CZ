import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import eu.dlask.finaceapp.Category
import eu.dlask.finaceapp.FinanceDatabase
import eu.dlask.finaceapp.R
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        val categoryInput: EditText = findViewById(R.id.category_input)
        val addCategoryButton: Button = findViewById(R.id.add_category_button)
        val categoryList: ListView = findViewById(R.id.category_list)

        val database = FinanceDatabase.getDatabase(this)
        val dao = database.financeDao()

        val categories = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        categoryList.adapter = adapter

        lifecycleScope.launch {
            val dbCategories = dao.getAllCategories()
            categories.addAll(dbCategories.map { it.name })
            adapter.notifyDataSetChanged()
        }

        addCategoryButton.setOnClickListener {
            val categoryName = categoryInput.text.toString()
            if (categoryName.isNotBlank()) {
                val newCategory = Category(name = categoryName)
                lifecycleScope.launch {
                    dao.insertCategory(newCategory)
                    categories.add(categoryName)
                    adapter.notifyDataSetChanged()
                }
                categoryInput.text.clear()
            }
        }
    }
}
