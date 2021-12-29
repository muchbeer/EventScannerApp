package com.muchbeer.eventscanner

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.muchbeer.eventscanner.databinding.FragmentCameraOneBinding

class FragmentCameraOne : Fragment() {

    private lateinit var binding : FragmentCameraOneBinding
    var hide: Animation? = null
    var reveal: Animation? = null

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var inputImage: InputImage
    private lateinit var barcodeScanner: BarcodeScanner

    private val TAG = FragmentCameraOne::class.qualifiedName


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraOneBinding.inflate(inflater, container, false)

        hide = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        reveal = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)

        binding.txtTitle.startAnimation(reveal)
        scanQrCodeSetting()

        binding.btnScanCode.setOnClickListener {

            val options = arrayOf("camera", "gallery")

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Pick an option")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                if (which == 0) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(cameraIntent)
                } else {
                    val storageIntent = Intent()
                    storageIntent.setType("image/*")
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT)
                    galleryLauncher.launch(storageIntent)
                }
            })

            builder.show()
        }

        return binding.root
    }


        private fun scanQrCodeSetting() {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC)
                .build()

            barcodeScanner = BarcodeScanning.getClient(options)
    }

    private fun processQr() {
        barcodeScanner.process(inputImage).addOnSuccessListener { barcodeList ->
            //handle barcode list
            barcodeList.forEach {
                val valueType = it.valueType
                when (valueType) {
                    Barcode.TYPE_WIFI -> {
                        val ssid = it.wifi!!.ssid
                        val password = it.wifi!!.password
                        val type = it.wifi!!.encryptionType

                        binding.txtTitle.text = "ssid is: ${ssid} \npassword is : ${password} \ntype is : ${type}"
                    }
                    Barcode.TYPE_URL -> {
                        val title = it.url!!.title
                        val url = it.url!!.url
                        binding.txtTitle.text = "title is: ${title} \nurl is : ${url}"

                    }
                    Barcode.TYPE_TEXT -> {
                        val text = it.displayValue
                        binding.txtTitle.text = "Your name is : ${text}"
                    }
                }

            }
        }.addOnFailureListener {
            Log.d(TAG, "QR error message is : ${it.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestForPermissions.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )


    }

    private val requestForPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    showToast("Permission Granted.")
                    cameraLauncher = registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                        object : ActivityResultCallback<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult?) {
                                val data = result?.data
                                try {
                                    val photo = data?.extras?.get("data") as Bitmap
                                    inputImage = InputImage.fromBitmap(photo, 0)
                                    processQr()
                                } catch (e: Exception) {
                                    Log.d(TAG, "onActivityResult: " + e.message)
                                }
                            }

                        }
                    )

                    galleryLauncher = registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult(),
                        object : ActivityResultCallback<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult?) {
                                val data = result?.data
                                inputImage = InputImage.fromFilePath(requireContext(), data?.data)
                                processQr()
                            }
                        }
                    )

                } else {
                    showToast("Permission Denied.")
                    //crying mainly I'd imagine
                }
            }

        }
}