package com.edusmart.recycler

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.edusmart.recycler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraViewModel: CameraViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(viewBinding.root)

        //val btn: Button = findViewById(R.id.button)
        if (!allPermissionsGranted())  {
            ActivityCompat.requestPermissions(
                this, MainActivity.REQUIRED_PERMISSIONS, MainActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        viewBinding.btnPlay.setOnClickListener(View.OnClickListener {
            iniciarCamera()
        })
        viewBinding.btnHelp.setOnClickListener(View.OnClickListener {
            iniciarHelp()
        })
        viewBinding.btnAbout.setOnClickListener(View.OnClickListener {
            iniciarAbout()
        })

        cameraViewModel = ViewModelProvider(this)[CameraViewModel::class.java]
    }
    private fun allPermissionsGranted() = MainActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun iniciarCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun iniciarAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    private fun iniciarHelp() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }
    private fun iniciarImersao(){
        val intent = Intent(this, VRActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
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
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}