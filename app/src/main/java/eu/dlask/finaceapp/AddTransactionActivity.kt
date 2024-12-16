package eu.dlask.finaceapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val transactionInput: EditText = findViewById(R.id.transaction_input)
        val saveButton: Button = findViewById(R.id.save_transaction_button)

        val database = FinanceDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()

        saveButton.setOnClickListener {
            val input = transactionInput.text.toString().trim()
            val parts = input.split(" - $", limit = 2) // Limit to 2 parts

            if (parts.size == 2) {
                val description = parts[0].trim()
                val amount = parts[1].trim().toDoubleOrNull()

                if (amount != null && description.isNotEmpty()) {
                    val transaction = Transaction(description = description, amount = amount)
                    lifecycleScope.launch {
                        transactionDao.insertTransaction(transaction)
                        finish()
                    }
                } else {
                    // Show error message to the user, e.g., using a Toast or Snackbar
                    Toast.makeText(this, "Invalid input format. Please use 'Description - '$'Amount'", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid input format. Please use 'Description - '$'Amount'", Toast.LENGTH_SHORT).show()
            }
        }
    }
}