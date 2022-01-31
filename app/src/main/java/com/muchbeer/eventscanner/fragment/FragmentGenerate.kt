package com.muchbeer.eventscanner.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muchbeer.eventscanner.databinding.FragmentGenerateBinding
import com.google.zxing.BarcodeFormat

import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception


class FragmentGenerate : Fragment() {

    private lateinit var binding : FragmentGenerateBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentGenerateBinding.inflate(inflater, container, false)

        binding.buttonGenerate.setOnClickListener {
            generateQr()
        }
        return binding.root
    }

    private fun generateQr() {
        val inputText = binding.edtGenerator.text.toString()
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(inputText, BarcodeFormat.QR_CODE, 400, 400)
            binding.imgGenerator.setImageBitmap(bitmap)
         } catch (e: Exception) {
        }
    }
}