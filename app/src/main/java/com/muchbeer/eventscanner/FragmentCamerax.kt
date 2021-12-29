package com.muchbeer.eventscanner

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    private lateinit var viewModel : ScanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraxBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        cameraExecutor = Executors.newSingleThreadExecutor()


        viewModel.progressState.observe(viewLifecycleOwner, {
            binding.scanBarcodeProgressBar.visibility = if(it) View.VISIBLE else View.GONE
        })

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestForPermissions.launch(android.Manifest.permission.CAMERA)
    }

    private val requestForPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                showMessage("Camera Permission is not granted")
            }
        }


    private fun startCamera() {
        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner.
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
    }
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}