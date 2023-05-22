package com.edusmart.recycler

import androidx.camera.view.PreviewView

interface CameraPreviewListener {
    fun onCameraPreviewAvailable(previewView: PreviewView)
}