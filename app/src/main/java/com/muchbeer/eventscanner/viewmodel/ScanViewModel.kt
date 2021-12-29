package com.muchbeer.eventscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.muchbeer.eventscanner.FragmentCameraxDirections
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    private val _progressState = MutableLiveData<Boolean>()
    val progressState : LiveData<Boolean>
            get() = _progressState

    private val _codeResultState = MutableStateFlow<String>("")
    val codeResultStatee : StateFlow<String>
        get() = _codeResultState.asStateFlow()


    private val _navigation = MutableStateFlow<NavDirections?>(null)
    val navigation: StateFlow<NavDirections?>
                get() = _navigation.asStateFlow()

    init {
        _progressState.value = false
    }

    fun searchBarCodeResult(barcode : String) {
        _progressState.value = true
            viewModelScope.launch {
                delay(1000)
                _codeResultState.value = barcode
            //    _navigation.emit(FragmentCameraxDirections.fragmentCameraxToFragmentSuccessScan(barcode))
                _progressState.value = false
            }

    }
}