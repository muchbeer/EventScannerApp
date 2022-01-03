package com.muchbeer.eventscanner.util

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.muchbeer.eventscanner.BarcodeListener
import com.muchbeer.eventscanner.R

class BarcodeAnalyzer(private val barcodeListener: BarcodeListener) : ImageAnalysis.Analyzer {


    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC)
        .build()

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to the scanner and have it do its thing
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    barcodes.forEach {
                      // barcodeListener(it.displayValue!!)
                        scanOption(it)
                    }

                }
                .addOnFailureListener {
                    // You should really do something about Exceptions
                }
                .addOnCompleteListener {
                    // It's important to close the imageProxy
                    imageProxy.close()
                }
        }

    }

    private fun scanOption(barcode : Barcode) {

        val valueType = barcode.valueType
        // See API reference for complete list of supported types
        when (valueType) {
            Barcode.TYPE_WIFI -> {
                val ssid = barcode.wifi!!.ssid
                val password = barcode.wifi!!.password
                val type = barcode.wifi!!.encryptionType
                val allTogether : String by lazy {
                    Resources.getSystem().getString(R.string.wifi_details, ssid, password) }
              //  Resources.getSystem().getString(R.string.btn_yes)
                barcodeListener("Wifi: ${allTogether}")
            }
            Barcode.TYPE_URL -> {
                val title = barcode.url!!.title
                val url = barcode.url!!.url
            }

            Barcode.TYPE_TEXT -> {
                val text = barcode.displayValue
                barcodeListener("The text is : ${text}")
            }

            Barcode.TYPE_PHONE -> {
                val phone_number = barcode.phone
            }

            Barcode.TYPE_PRODUCT -> {
                val product_name = barcode.displayValue
                barcodeListener("The text is : ${product_name}")
            }

        }
    }
}