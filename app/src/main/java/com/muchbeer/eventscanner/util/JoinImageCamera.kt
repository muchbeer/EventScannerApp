package com.muchbeer.eventscanner.util

import androidx.camera.core.ImageAnalysis

interface JoinImageCamera {

    fun imageAnalyser() : ImageAnalysis.Analyzer

    fun galleryAnalyser() 
}