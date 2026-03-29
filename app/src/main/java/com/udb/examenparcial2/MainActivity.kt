package com.udb.examenparcial2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.udb.examenparcial2.databinding.ActivityMainBinding
import com.udb.examenparcial2.ui.auth.AuthViewModel
import com.udb.examenparcial2.ui.auth.LoginActivity
import com.udb.examenparcial2.ui.catalog.DestinationAdapter
import com.udb.examenparcial2.ui.catalog.DestinationViewModel
import com.udb.examenparcial2.ui.catalog.EditDestinationActivity
import com.udb.examenparcial2.util.Resource

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var destinationViewModel: DestinationViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var adapter: DestinationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        destinationViewModel = ViewModelProvider(this)[DestinationViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = DestinationAdapter { destination ->
            val intent = Intent(this, EditDestinationActivity::class.java).apply {
                putExtra("DESTINATION_ID", destination.id)
                putExtra("DESTINATION_NAME", destination.name)
                putExtra("DESTINATION_COUNTRY", destination.country)
                putExtra("DESTINATION_PRICE", destination.price)
                putExtra("DESTINATION_DESCRIPTION", destination.description)
                putExtra("DESTINATION_IMAGE_URL", destination.imageUrl)
            }
            startActivity(intent)
        }
        binding.rvDestinations.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditDestinationActivity::class.java))
        }
    }

    private fun observeViewModel() {
        destinationViewModel.destinations.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = resource.data ?: emptyList()
                    adapter.submitList(data)
                    binding.tvEmpty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Handle error (e.g., Snackbar)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_logout) {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
