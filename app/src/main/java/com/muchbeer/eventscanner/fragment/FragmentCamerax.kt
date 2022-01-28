package com.muchbeer.eventscanner.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.muchbeer.eventscanner.databinding.FragmentCameraxBinding
import com.muchbeer.eventscanner.util.BarcodeAnalyzer
import com.muchbeer.eventscanner.viewmodel.ScanViewModel
import kotlinx.coroutines.flow.collectLatest
import logcat.logcat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

typealias BarcodeListener = (barcode: String) -> Unit
class FragmentCamerax : Fragment() {

    private lateinit var binding : FragmentCameraxBinding
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var inputImage: InputImage
    private lateinit var barcodeScanner: BarcodeScanner

    private lateinit var viewModel : ScanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraxBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        cameraExecutor = Executors.newSingleThreadExecutor()

        barcodeScanner = BarcodeScanning.getClient()

        setOption()

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {  result : ActivityResult? ->
            val dataUri = result?.data?.data
            logcat { "Entered Gallery registerForActivity" }
            dataUri?.let {
                logcat { "The uri produced not null is : ${it.path}" }
                inputImage = InputImage.fromFilePath(requireContext(), it )
                BarcodeAnalyzer{ receiveBarcode->
                    logcat { "Successful call the image from gallery" }
                    viewModel.searchBarCodeResult(receiveBarcode)
                    logcat { "the code is : ${receiveBarcode}" }
                }.galleryImageAnalyser(inputImage)
             //   processQr()

            }
        }

        viewModel.progressState.observe(viewLifecycleOwner, {
            binding.scanBarcodeProgressBar.visibility = if(it) View.VISIBLE else View.GONE
        })

        lifecycleScope.launchWhenStarted {
            viewModel.scanNewCode.collectLatest {
                binding.btnScanAgain.visibility = if(it) View.VISIBLE else View.GONE
            }
        }

        binding.btnScanAgain.setOnClickListener {
            binding.apply {
                btnScanAgain.visibility =  View.GONE
                tvResult.text = ""
            }

        }

        viewModel.navigation.observe(viewLifecycleOwner) { navDirection ->
            navDirection?.let {
                findNavController().navigate(it)
            }
        }
   /*    lifecycleScope.launchWhenStarted {
            viewModel.navigation.collectLatest { navDirection->
                navDirection?.let {
                    findNavController().navigate(it)
                }
            }
        }*/

        lifecycleScope.launchWhenStarted {
            viewModel.codeResultStatee.collectLatest {
                binding.tvResult.text = it
            }
        }

        binding.btnCreateQ.setOnClickListener {
           findNavController().navigate(FragmentCameraxDirections.fragmentCameraxToFragmentCreateQr())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  requestForPermissions.launch(android.Manifest.permission.CAMERA)
        requestForPermissions.launch(
            arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        )


    }

    private val requestForPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                   // startCamera()
                   //    showMessage("${permissionName} is Granted")
                    logcat { "${permissionName} is Granted" }
                }  else {
                    showMessage("${permissionName} Permission is not granted")
                   // logcat { "Permision ${permissionName} not granted" }
                }

            }

        }



    private fun startCamera() {
        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner.
        logcat { "Enter the startCamera" }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Add a listener to the cameraProviderFuture.
        // The first argument is a Runnable, which will be where the magic actually happens.
        // The second argument (way down below) is an Executor that runs on the main thread.
        cameraProviderFuture.addListener({
            // Add a ProcessCameraProvider, which binds the lifecycle of your camera to
            // the LifecycleOwner within the application's life.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Initialize the Preview object, get a surface provider from your PreviewView,
            // and set it on the preview instance.
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    binding.scanBarcodePreviewView.surfaceProvider

                )
            }
            // Setup the ImageAnalyzer for the ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        if (processingBarcode.compareAndSet(false, true)) {
                            viewModel.searchBarCodeResult(barcode)
                            logcat { "the code is : ${barcode}" }

                        }
                    })
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind any bound use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to lifecycleOwner
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                logcat { "Binding failed! : $e" }
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()

        processingBarcode.set(false)
        viewModel.triggerScanNext()
    }


    private fun setOption() {
        val options = arrayOf("camera", "gallery")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pick an option")
        builder.setItems(options)  { dialog, which ->
            if (which == 0) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    showMessage("User turn down please try again ")

                }

            } else {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    val storageIntent = Intent()
                    storageIntent.setType("image/*")
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT)
                    galleryLauncher.launch(storageIntent)
                } else {
                    showMessage("User turned down Please try again")
                }

            }
        }
        builder.show()
    }

}