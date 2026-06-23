package com.example.agriguard.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.agriguard.R
import com.example.agriguard.databinding.ActivityDiseaseDetectionBinding
import com.example.agriguard.models.PredictionResult
import com.example.agriguard.utils.BitmapUtils
import com.example.agriguard.viewmodels.DetectionViewModel
import android.util.Log
import com.example.agriguard.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DiseaseDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseDetectionBinding
    private lateinit var viewModel: DetectionViewModel
    private var currentImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentImageUri?.let { showPreview(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { showPreview(it) }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(DetectionViewModel::class.java)

        if (BuildConfig.PLANT_ID_API_KEY.isEmpty()) {
            Toast.makeText(this, "Plant.id API Key is missing!", Toast.LENGTH_LONG).show()
        }

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnCapture.setOnClickListener { checkCameraPermission() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        binding.btnDetect.setOnClickListener { startDetection() }
        binding.btnRemoveImage.setOnClickListener { resetUI() }
        binding.btnTryAgain.setOnClickListener { resetUI() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading: Boolean ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnDetect.isEnabled = !loading
            if (loading) {
                binding.resultLayout.visibility = View.GONE
                binding.errorCard.visibility = View.GONE
            }
        }

        viewModel.error.observe(this) { error: String? ->
            if (error != null) {
                showError("❌ Analysis Error", error)
            }
        }

        viewModel.result.observe(this) { result: PredictionResult? ->
            if (result != null) {
                if (result.status == "Invalid") {
                    showError("❌ Invalid Crop Image", result.description)
                } else {
                    displayResult(result)
                }
            }
        }
    }

    private fun showError(title: String, message: String) {
        binding.errorCard.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.GONE
        binding.tvErrorTitle.text = title
        binding.tvErrorMessage.text = message
    }

    private fun displayResult(result: PredictionResult) {
        // Requirement 3 & 5: Clear previous state and show layout
        binding.resultLayout.visibility = View.VISIBLE
        binding.errorCard.visibility = View.GONE

        val isHealthy = result.status.equals("Healthy", ignoreCase = true)

        binding.tvResultStatus.text = result.status
        binding.tvConfidence.text = result.getConfidenceString()
        
        // Requirement 2 & 3: Handle Unknown Crop and Warning Badge
        if (!result.isCropIdentified) {
            binding.tvResultCrop.text = "Crop: Unknown Crop"
            binding.tvCropWarning.visibility = View.VISIBLE
        } else {
            binding.tvResultCrop.text = "Crop: ${result.cropName}"
            binding.tvCropWarning.visibility = View.GONE
        }

        binding.tvResultDisease.text = if (isHealthy) "None" else result.diseaseName
        
        // UI Styling based on status
        if (isHealthy) {
            binding.tvResultStatus.setBackgroundResource(R.drawable.bg_status_healthy)
            binding.tvResultDisease.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
        } else {
            binding.tvResultStatus.setBackgroundResource(R.drawable.bg_status_diseased)
            binding.tvResultDisease.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }

        // Requirement 1: ALWAYS show details, even if healthy
        binding.cardDetails.visibility = View.VISIBLE
        binding.cardRecommendations.visibility = View.VISIBLE
        binding.cardPrevention.visibility = View.VISIBLE
        
        // Populating text fields
        binding.tvSymptoms.text = result.symptoms
        binding.tvCauses.text = result.causes
        binding.tvFertilizers.text = result.fertilizer
        binding.tvPesticides.text = result.pesticides
        binding.tvPrevention.text = result.preventionTips
        binding.tvIrrigation.text = result.irrigationTips
    }

    private fun startDetection() {
        currentImageUri?.let { uri ->
            try {
                val bitmap = BitmapUtils.getCorrectlyOrientedBitmap(this, uri)
                viewModel.detect(bitmap)
            } catch (e: IOException) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPreview(uri: Uri) {
        currentImageUri = uri
        try {
            val bitmap = BitmapUtils.getCorrectlyOrientedBitmap(this, uri)
            binding.ivPreview.setImageBitmap(bitmap)
        } catch (e: IOException) {
            binding.ivPreview.setImageURI(uri)
        }
        binding.ivPreview.visibility = View.VISIBLE
        binding.btnRemoveImage.visibility = View.VISIBLE
        binding.uploadPlaceholder.visibility = View.GONE
        binding.btnDetect.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.GONE
        binding.errorCard.visibility = View.GONE
    }

    private fun resetUI() {
        currentImageUri = null
        binding.ivPreview.setImageURI(null)
        binding.ivPreview.visibility = View.GONE
        binding.btnRemoveImage.visibility = View.GONE
        binding.uploadPlaceholder.visibility = View.VISIBLE
        binding.btnDetect.visibility = View.GONE
        binding.resultLayout.visibility = View.GONE
        binding.errorCard.visibility = View.GONE
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.agriguard.fileprovider",
                photoFile
            )
            currentImageUri = uri
            takePictureLauncher.launch(uri)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
}
