package com.muchbeer.eventscanner.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import logcat.logcat

class CreateQrCodeViewModel : ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap : LiveData<Bitmap>
        get() = _bitmap

    fun createQrCode(inputText : String) = viewModelScope.launch {
        if (inputText.isEmpty()) {
           // Toast.makeText(requireContext(), "Require Text ", Toast.LENGTH_LONG).show()
            logcat { "Require text" }
        } else {
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(inputText, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height

                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for(x in 0 until width) {
                    for(y in 0 until height) {
                      bmp.setPixel(x,y, if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                        _bitmap.value = bmp
                    }
                }

                //img_ivQrCode.setImageBitmap(bmp)
            } catch (e: WriterException) {
                logcat { "The failed creator is : ${e.message}"}
            }
        }

    }
}