package com.muchbeer.eventscanner.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.muchbeer.eventscanner.databinding.FragmentCreateQrBinding
import com.muchbeer.eventscanner.viewmodel.CreateQrCodeViewModel
import logcat.logcat

class FragmentCreateQr : Fragment() {

    private lateinit var binding: FragmentCreateQrBinding

    private val viewModel : CreateQrCodeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCreateQrBinding.inflate(inflater, container, false)


        binding.apply {

            btnCreateQr.setOnClickListener {
                val inputText = inputTextCreater.text.toString().trim()
                viewModel.createQrCode(inputText)

                viewModel.bitmap.observe(viewLifecycleOwner) {
                  /*  if (it !=null) {
                        btnCreateQr.visibility = View.INVISIBLE
                    } else */
                    imgQrCreator.setImageBitmap(it)
                }

            }
        }

      return  binding.root
    }

    private fun needTobeRemoved(inputText : String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(inputText, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }

            binding.imgQrCreator.setImageBitmap(bmp)
        } catch (e: WriterException) {
            logcat { "The failed creator is : ${e.message}" }
        }
    }
}

