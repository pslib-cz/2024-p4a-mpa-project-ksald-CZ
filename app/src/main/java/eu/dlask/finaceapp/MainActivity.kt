package eu.dlask.finaceapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val transactionDescriptions = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var financeDao: FinanceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = FinanceDatabase.getDatabase(this)
        financeDao = database.financeDao()

        val addTransactionButton: Button = findViewById(R.id.add_transaction_button)
        val transactionList: ListView = findViewById(R.id.transaction_list)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            transactionDescriptions
        )
        transactionList.adapter = adapter

        // Load transactions from the database
        loadTransactions()

        addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload transactions when returning to this activity
        loadTransactions()
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            val transactions = financeDao.getAllTransactions()
            transactionDescriptions.clear()
            transactionDescriptions.addAll(transactions.map { "${it.description} - $${it.amount}" })
            adapter.notifyDataSetChanged()
        }
    }
}