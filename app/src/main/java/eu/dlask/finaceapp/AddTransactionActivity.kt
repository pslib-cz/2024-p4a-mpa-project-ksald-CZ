package eu.dlask.finaceapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val descriptionInput: EditText = findViewById(R.id.transaction_input)
        val amountInput: EditText = findViewById(R.id.transaction_amount)
        val categorySpinner: Spinner = findViewById(R.id.category_spinner)
        val saveButton: Button = findViewById(R.id.save_transaction_button)
        val incomeCheckbox: CheckBox = findViewById(R.id.transaction_income_checkbox)

        val database = FinanceDatabase.getDatabase(this)
        val transactionDao = database.financeDao()

        // Fetch and populate categories in the spinner
        lifecycleScope.launch {
            val categories = transactionDao.getAllCategories()
            val categoryNames = categories.map { it.name }.toMutableList()

            // Add a "New Category" option
            categoryNames.add("Add New Category")

            val spinnerAdapter = ArrayAdapter(
                this@AddTransactionActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter

            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (categoryNames[position] == "Add New Category") {
                        showAddCategoryDialog(transactionDao, categoryNames, spinnerAdapter)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        saveButton.setOnClickListener {
            val description = descriptionInput.text.toString().trim()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val selectedCategory = categorySpinner.selectedItem?.toString()
            val isIncome = incomeCheckbox.isChecked

            if (description.isNotBlank() && amount != null && selectedCategory != null && selectedCategory != "Add New Category") {
                lifecycleScope.launch {
                    val categoryId = transactionDao.getAllCategories().find { it.name == selectedCategory }?.id
                    val transaction = Transaction(
                        description = description,
                        amount = amount,
                        isIncome = isIncome,
                        categoryId = categoryId
                    )
                    transactionDao.insertTransaction(transaction)
                    Toast.makeText(this@AddTransactionActivity, "Transaction saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select a valid category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddCategoryDialog(
        transactionDao: FinanceDao,
        categoryNames: MutableList<String>,
        spinnerAdapter: ArrayAdapter<String>
    ) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Add New Category")

        val input = EditText(this)
        input.hint = "Category Name"
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Add") { _, _ ->
            val newCategoryName = input.text.toString().trim()
            if (newCategoryName.isNotBlank()) {
                lifecycleScope.launch {
                    val newCategory = Category(name = newCategoryName)
                    transactionDao.insertCategory(newCategory)
                    categoryNames.add(categoryNames.size - 1, newCategoryName) // Add before "Add New Category"
                    spinnerAdapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }
}