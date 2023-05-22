package com.edusmart.recycler

import androidx.camera.core.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    private val _cameraPreview: MutableLiveData<Preview.SurfaceProvider?> = MutableLiveData()
    val cameraPreview: LiveData<Preview.SurfaceProvider?> = _cameraPreview

    fun setCameraPreview(surfaceProvider: Preview.SurfaceProvider?) {
        _cameraPreview.value = surfaceProvider
    }
}