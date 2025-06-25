package com.example.tugas3api

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView


import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var btnGetUsers: Button
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnGetProducts: Button
    private lateinit var tvUserName: TextView
    private lateinit var tvUserUniv: TextView
    private lateinit var tvAveragePrice: TextView

    private var users = listOf<User>()
    private var currentUserIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGetUsers = findViewById(R.id.btnGetUsers)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnGetProducts = findViewById(R.id.btnGetProducts)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserUniv = findViewById(R.id.tvUserUniv)
        tvAveragePrice = findViewById(R.id.tvAveragePrice)

        btnGetUsers.setOnClickListener { getUsers() }
        btnPrevious.setOnClickListener { showUser(currentUserIndex - 1) }
        btnNext.setOnClickListener { showUser(currentUserIndex + 1) }
        btnGetProducts.setOnClickListener { getAverageProductPrice() }

        updateNavButtons()
    }

    private fun getUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL("https://dummyjson.com/users").readText()
                val jsonObject = JSONObject(response)
                val usersJson = jsonObject.getJSONArray("users")
                val tempList = mutableListOf<User>()
                for (i in 0 until usersJson.length()) {
                    val obj = usersJson.getJSONObject(i)
                    val name = "${obj.getString("firstName")} ${obj.getString("lastName")}"
                    val univ = obj.optString("university", "-")
                    tempList.add(User(name, univ))
                }
                users = tempList.sortedBy { it.name }
                withContext(Dispatchers.Main) {
                    showUser(0)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvUserName.text = "Error fetching users"
                    tvUserUniv.text = ""
                }
            }
        }
    }

    private fun showUser(index: Int) {
        if (users.isEmpty()) return
        currentUserIndex = index.coerceIn(0, users.size - 1)
        val user = users[currentUserIndex]
        tvUserName.text = user.name
        tvUserUniv.text = user.univ
        updateNavButtons()
    }

    private fun updateNavButtons() {
        btnPrevious.isEnabled = currentUserIndex > 0
        btnNext.isEnabled = users.isNotEmpty() && currentUserIndex < users.size - 1
    }

    private fun getAverageProductPrice() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL("https://dummyjson.com/products").readText()
                val products = JSONObject(response).getJSONArray("products")
                var total = 0.0
                for (i in 0 until products.length()) {
                    total += products.getJSONObject(i).getDouble("price")
                }
                val avg = if (products.length() > 0) total / products.length() else 0.0
                val avgFormatted = formatRupiah(avg)
                withContext(Dispatchers.Main) {
                    tvAveragePrice.text = "Harga rata-rata: $avgFormatted"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvAveragePrice.text = "Gagal mengambil data produk!"
                }
            }
        }
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(number)
    }

    data class User(val name: String, val univ: String)
}