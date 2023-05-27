package com.edusmart.recycler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
}