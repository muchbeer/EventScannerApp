package com.muchbeer.eventscanner.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muchbeer.eventscanner.databinding.FragmentSuccessScanBinding

class FragmentSuccessScan : Fragment() {

private lateinit var binding : FragmentSuccessScanBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSuccessScanBinding.inflate(inflater, container, false)

        val resultDisplay : FragmentSuccessScanArgs by navArgs()

        binding.tvResultSuccess.text = resultDisplay.resultCode
        binding.btnBack.setOnClickListener {
            findNavController().navigate(FragmentSuccessScanDirections.fragmentSuccessScanToFragmentCamerax2())
        }

        binding.btnScanImage.setOnClickListener {
            findNavController().navigate(FragmentSuccessScanDirections.fragmentSuccessScanToFragmentGenerate())
        }
        return binding.root
    }


}