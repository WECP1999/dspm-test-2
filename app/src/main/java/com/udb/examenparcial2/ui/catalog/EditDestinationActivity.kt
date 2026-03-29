package com.udb.examenparcial2.ui.catalog

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.udb.examenparcial2.R
import com.udb.examenparcial2.data.model.Destination
import com.udb.examenparcial2.databinding.ActivityEditDestinationBinding
import com.udb.examenparcial2.util.Resource
import java.io.ByteArrayOutputStream

class EditDestinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDestinationBinding
    private lateinit var viewModel: DestinationViewModel
    private var selectedImageUri: Uri? = null
    private var destinationId: String? = null
    private var currentImageUrl: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivDestination.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DestinationViewModel::class.java]

        destinationId = intent.getStringExtra("DESTINATION_ID")
        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        if (destinationId != null) {
            title = getString(R.string.edit_destination_title)
            binding.btnDelete.visibility = View.VISIBLE
            binding.etName.setText(intent.getStringExtra("DESTINATION_NAME"))
            binding.etPrice.setText(intent.getDoubleExtra("DESTINATION_PRICE", 0.0).toString())
            binding.etDescription.setText(intent.getStringExtra("DESTINATION_DESCRIPTION"))
            
            currentImageUrl = intent.getStringExtra("DESTINATION_IMAGE_URL")
            if (!currentImageUrl.isNullOrEmpty()) {
                val imageSource: Any = if (currentImageUrl!!.length > 200) {
                    try {
                        Base64.decode(currentImageUrl, Base64.DEFAULT)
                    } catch (e: Exception) {
                        currentImageUrl!!
                    }
                } else {
                    currentImageUrl!!
                }

                Glide.with(this)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.ivDestination)
            }

            val country = intent.getStringExtra("DESTINATION_COUNTRY")
            val adapter = binding.spinnerCountry.adapter as? ArrayAdapter<String>
            val position = adapter?.getPosition(country) ?: -1
            if (position >= 0) {
                binding.spinnerCountry.setSelection(position)
            }
        } else {
            title = getString(R.string.create_destination_title)
        }
    }

    private fun setupListeners() {
        binding.btnSelectImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            saveDestination()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun saveDestination() {
        val name = binding.etName.text.toString().trim()
        val country = binding.spinnerCountry.selectedItem.toString()
        val priceStr = binding.etPrice.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        
        var imageUrlToSave = currentImageUrl ?: ""
        
        // Convert selected image to Base64 for "uploading" without external storage
        selectedImageUri?.let { uri ->
            val base64 = uriToBase64(uri)
            if (base64 != null) {
                imageUrlToSave = base64
            }
        }

        val destination = Destination(
            id = destinationId ?: "",
            name = name,
            country = country,
            price = price,
            description = description,
            imageUrl = imageUrlToSave
        )
        Log.d("Imagen", imageUrlToSave)
        viewModel.saveDestination(destination, imageUrlToSave, destinationId != null)
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val outputStream = ByteArrayOutputStream()
            // Compress and potentially resize to stay within Firestore document limits (1MB)
            val scaledBitmap = if (bitmap.width > 600) {
                val aspectRatio = bitmap.height.toDouble() / bitmap.width
                Bitmap.createScaledBitmap(bitmap, 600, (600 * aspectRatio).toInt(), true)
            } else bitmap
            
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setPositiveButton(R.string.confirm) { _, _ ->
                destinationId?.let { viewModel.deleteDestination(it) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.operationStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, R.string.success_save, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
                null -> {}
            }
        }
    }
}
