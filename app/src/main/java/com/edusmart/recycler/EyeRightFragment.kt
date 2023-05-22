package com.edusmart.recycler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.edusmart.recycler.databinding.FragmentEyeRightBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class EyeRightFragment : Fragment() , CameraPreviewListener {
    private lateinit var binding: FragmentEyeRightBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraPreviewView: PreviewView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEyeRightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar se já temos o PreviewView disponível
        if (cameraPreviewView != null) {
            setupCameraPreview()
        }
    }

    override fun onCameraPreviewAvailable(previewView: PreviewView) {
        cameraPreviewView = previewView
        // Verificar se a view já foi criada
        if (view != null) {
            setupCameraPreview()
        }
    }

    private fun setupCameraPreview() {
        // Use o PreviewView para configurar a visualização da câmera neste fragmento
        binding.viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        Log.d(TAG, "ENTROUUUU")
    //binding.viewFinder.setSurfaceProvider(cameraPreviewView?.surfaceProvider)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EyeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EyeRightFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private const val TAG = "EyeFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}