package com.example.myapplication

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    private val url = "https://fetch-hiring.s3.amazonaws.com/hiring.json"
    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var gson: Gson
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the variables
        requestQueue = Volley.newRequestQueue(this)
        gson = Gson()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching data....")

        // Fetch and display data
        fetchData()
    }

    private fun fetchData() {
        progressDialog.show()
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val fetchedHiringItems: List<HiringItem> = gson.fromJson(response.toString(),  Array<HiringItem>::class.java).toList()

                // Filter and sort the items
                val filteredItems = fetchedHiringItems.filter { !it.name.isNullOrBlank() }
                val sortedItems = filteredItems.sortedWith(
                    compareBy<HiringItem> { it.listId }.thenBy { it.name }
                )

                // Set up the RecyclerView adapter with sorted items
                recyclerView.adapter = RecyclerviewAdapter(sortedItems)
                progressDialog.hide()
            },
            { error ->
                progressDialog.hide()

                // Handle error
                Toast.makeText(this, "Error while fetching data: ${error.message}", Toast.LENGTH_LONG)
                    .show()
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog.hide()

        // Stop the request queue when activity is destroyed
        requestQueue.stop()
    }

}