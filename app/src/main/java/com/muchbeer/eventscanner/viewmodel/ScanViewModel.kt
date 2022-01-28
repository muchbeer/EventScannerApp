package com.muchbeer.eventscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.muchbeer.eventscanner.fragment.FragmentCameraxDirections
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    private val _progressState = MutableLiveData<Boolean>()
    val progressState : LiveData<Boolean>
            get() = _progressState

    // Backing property to avoid flow emissions from other classes
    private val _scanNewCode = MutableSharedFlow<Boolean>()
    val scanNewCode: SharedFlow<Boolean>
        get() = _scanNewCode.asSharedFlow()

    private val _codeResultState = MutableStateFlow<String>("")
    val codeResultStatee : StateFlow<String>
        get() = _codeResultState.asStateFlow()


    /*private val _navigation = MutableStateFlow<NavDirections?>(null)
    val navigation: StateFlow<NavDirections?>
                get() = _navigation.asStateFlow()*/

    private val _navigation = MutableLiveData<NavDirections?>(null)
    val navigation: LiveData<NavDirections?>
        get() = _navigation

    init {
        _progressState.value = false
    }

    fun triggerScanNext() {
        //alternative please look the documentation and see the best way of using hilt with coroutine
        viewModelScope.launch {
            _scanNewCode.emit(false)
        }
    }
    fun searchBarCodeResult(barcode : String) {
        _progressState.value = true
            viewModelScope.launch {
                delay(1000)
                _codeResultState.value = barcode
                _navigation.value = (FragmentCameraxDirections.fragmentCameraxToFragmentSuccessScan(barcode))
                _progressState.value = false
                _scanNewCode.emit(true)
            }
    }
}