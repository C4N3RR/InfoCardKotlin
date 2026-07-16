package com.example.infocard.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.infocard.data.CardProvider
import com.example.infocard.util.AppLanguage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CardScannerActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false

    // Voting Engine maps
    private val numberFrequencies = mutableMapOf<String, Int>()
    private val expiryFrequencies = mutableMapOf<String, Int>()
    private val providerFrequencies = mutableMapOf<CardProvider, Int>()
    private var framesProcessed = 0
    private val maxFrames = 35
    private val requiredNumberConfidence = 5
    private val requiredExpiryConfidence = 3

    // State exposed to Compose overlay
    private var scanProgressPercent by mutableStateOf(0)
    private var appLanguage by mutableStateOf(AppLanguage.TR)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan cards.", Toast.LENGTH_SHORT).show()
            finishWithCancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read language from launch intent
        val langStr = intent.getStringExtra("lang") ?: "tr"
        appLanguage = AppLanguage.fromValue(langStr)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CardScannerView(
                language = appLanguage,
                progressPercent = scanProgressPercent,
                onClose = { finishWithCancel() },
                onPreviewViewReady = { previewView ->
                    setupCameraWithPreview(previewView)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Request permission or start camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        // Will be initialized when Compose View is ready and binds previewView
    }

    private fun setupCameraWithPreview(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview usecase
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Text Recognizer (ML Kit)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            // Analysis Usecase
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy, recognizer)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CardScanner", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy,
        recognizer: com.google.mlkit.vision.text.TextRecognizer
    ) {
        if (isProcessing) {
            imageProxy.close()
            return
        }
        isProcessing = true

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val lines = visionText.textBlocks.flatMap { block -> block.lines.map { it.text } }
                    parseCardDetails(lines)
                    isProcessing = false
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    Log.e("CardScanner", "Text recognition failed", e)
                    isProcessing = false
                    imageProxy.close()
                }
        } else {
            isProcessing = false
            imageProxy.close()
        }
    }

    private fun parseCardDetails(strings: List<String>) {
        var foundNumber: String? = null
        var foundExpiry: String? = null

        // Number regex (13-16 digits with optional spaces or dashes)
        val numberRegex = Regex("\\b(?:\\d[ -]*?){13,16}\\b")
        // Expiry regex (MM/YY or MM/YYYY)
        val expiryRegex = Regex("\\b(0[1-9]|1[0-2])\\/([0-9]{2,4})\\b")

        for (str in strings) {
            val cleanStr = str.trim()

            // Card Number
            val numMatch = numberRegex.find(cleanStr)
            if (numMatch != null) {
                val digits = numMatch.value.filter { it.isDigit() }
                if (digits.length in 13..16) {
                    foundNumber = digits
                }
            }

            // Expiry Date
            val expMatch = expiryRegex.find(cleanStr)
            if (expMatch != null) {
                val matchVal = expMatch.value.replace(" ", "")
                val parts = matchVal.split("/")
                if (parts.size == 2) {
                    val month = parts[0]
                    var year = parts[1]
                    if (year.length == 4) {
                        year = year.substring(2)
                    }
                    foundExpiry = "$month/$year"
                }
            }
        }

        // Detect provider in OCR texts (keywords)
        var frameProvider = CardProvider.UNKNOWN
        for (str in strings) {
            val lower = str.lowercase()
            when {
                lower.contains("visa") -> {
                    frameProvider = CardProvider.VISA
                    break
                }
                lower.contains("mastercard") || lower.contains("master") -> {
                    frameProvider = CardProvider.MASTERCARD
                    break
                }
                lower.contains("troy") -> {
                    frameProvider = CardProvider.TROY
                    break
                }
            }
        }

        if (frameProvider == CardProvider.UNKNOWN && foundNumber != null) {
            frameProvider = CardProvider.detect(foundNumber)
        }

        // Accumulate frequencies
        foundNumber?.let { numberFrequencies[it] = (numberFrequencies[it] ?: 0) + 1 }
        foundExpiry?.let { expiryFrequencies[it] = (expiryFrequencies[it] ?: 0) + 1 }
        if (frameProvider != CardProvider.UNKNOWN) {
            providerFrequencies[frameProvider] = (providerFrequencies[frameProvider] ?: 0) + 1
        }

        framesProcessed++

        // Update progress percentage
        val percent = ((framesProcessed.toDouble() / maxFrames.toDouble()) * 100).toInt().coerceAtMost(99)
        scanProgressPercent = percent

        // Extract high confidence results
        val bestNumberEntry = numberFrequencies.maxByOrNull { it.value }
        val bestExpiryEntry = expiryFrequencies.maxByOrNull { it.value }
        val bestProvider = providerFrequencies.maxByOrNull { it.value }?.key ?: CardProvider.UNKNOWN

        val hasConfidentNumber = (bestNumberEntry?.value ?: 0) >= requiredNumberConfidence
        val hasConfidentExpiry = (bestExpiryEntry?.value ?: 0) >= requiredExpiryConfidence

        if ((hasConfidentNumber && hasConfidentExpiry) || framesProcessed >= maxFrames) {
            finishWithResult(bestNumberEntry?.key, bestExpiryEntry?.key, bestProvider)
        }
    }

    private fun finishWithResult(number: String?, expiry: String?, provider: CardProvider) {
        val data = Intent().apply {
            putExtra("number", number)
            putExtra("expiry", expiry)
            putExtra("provider", provider.value)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun finishWithCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
