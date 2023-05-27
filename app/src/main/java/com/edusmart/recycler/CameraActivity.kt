package com.edusmart.recycler

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.edusmart.recycler.databinding.ActivityCameraBinding
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min
import java.util.Random

class CameraActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewBinding: ActivityCameraBinding
    private lateinit var bitmapBuffer: Bitmap
    private var imageAnalyzer: ImageAnalysis? = null
    var mMediaPlayer: MediaPlayer? = null
    private var score: Float = 0.0f
    private var label: String = ""
    private var residuo_para_procurar: Array<String> = arrayOf("organic", "recycled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        var indexClasse:Int = rand(1,2) - 1
        setContentView(viewBinding.root)
        viewBinding.textViewFind.text = String.format("Procure por um resíduo %s.", residuo_para_procurar[indexClasse])

        imageClassifierHelper =
            ImageClassifierHelper(context = baseContext, imageClassifierListener = this)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, CameraActivity.REQUIRED_PERMISSIONS, CameraActivity.REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewBinding.imageButton.setOnClickListener(View.OnClickListener {
            if(verificarResultado(residuo_para_procurar[indexClasse])){
                viewBinding.textViewFind.text = String.format("Correto!!")
            }else{
                viewBinding.textViewFind.text = String.format("Incorreto!!")
            }

            if(score < 0.50f){
                stopSound()
                playSound(R.raw.fail)
            }else{
                stopSound()
                Log.d(TAG, label)
                if(this.label.trim() == "recycled"){
                    Log.d(TAG, "recycled")
                    playSound(R.raw.recycled)
                }else if(this.label.trim() == "organic"){
                    Log.d(TAG, "ORGANICO")
                    playSound(R.raw.organic)
                }
            }
        })
    }
    fun verificarResultado(tipo_residuo: String): Boolean{
        return (this.label.trim().equals(tipo_residuo))
    }
    fun rand(from: Int, to: Int) : Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }
    private fun allPermissionsGranted() = CameraActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun setImage(imgV: ImageView, bmp: Bitmap) {
        this.runOnUiThread(Runnable {
            imgV.setImageBitmap(bmp)
        })
    }

    fun playContentUri(uri: Uri) {
        var mMediaPlayer: MediaPlayer? = null
        try {
            mMediaPlayer = MediaPlayer().apply {
                setDataSource(application, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                )
                prepare()
                start()
            }
        } catch (exception: IOException) {
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

    // 1. Plays the water sound
    fun playSound(resource: Int) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this,resource)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
            Log.d(TAG, "mMediaPlayer == null")
        } else {
            Log.d(TAG, "else (mMediaPlayer != null)")
            mMediaPlayer!!.start()
        }
    }

    // 2. Pause playback
    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }

    // 3. Stops playback
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    // 4. Destroys the MediaPlayer instance when the app is closed
    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }


            // ImageAnalysis. Using RGBA 8888 to match how our models work
            imageAnalyzer =
                ImageAnalysis.Builder()
                    //.setTargetResolution(Size(200,200))
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(viewBinding.viewFinder.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    // The analyzer can then be assigned to the instance
                    .also {
                        it.setAnalyzer(cameraExecutor) { image ->
                            if (!::bitmapBuffer.isInitialized) {
                                // The image rotation and RGB image buffer are initialized only once
                                // the analyzer has started running

                                bitmapBuffer = Bitmap.createBitmap(
                                    image.width,
                                    image.height,
                                    Bitmap.Config.ARGB_8888
                                )
                            }
                            //this.setImage(viewBinding.viewFinder2, bitmapBuffer)
                            classifyImage(image)
                        }
                    }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                var useCases = arrayOf(preview, imageAnalyzer)
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, *useCases)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun classifyImage(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        // Pass Bitmap and rotation to the image classifier helper for processing and classification
        imageClassifierHelper.classify(bitmapBuffer, getScreenOrientation())
    }

    private fun getScreenOrientation() : Int {
        val outMetrics = DisplayMetrics()

        val display: Display?
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            display = this.display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }

        return display?.rotation ?: 0
    }

    companion object {
        private const val TAG = "CameraActivity"
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

    override fun onError(error: String) {
        this.runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            //classificationResultsAdapter.updateResults(null)
            //classificationResultsAdapter.notifyDataSetChanged()
        }
    }

    private var categories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    override fun onResults(
        results: List<Classifications>?,
        inferenceTime: Long
    ) {
        this.runOnUiThread {
            // Show result on bottom sheet
            //classificationResultsAdapter.updateResults(results)
            //classificationResultsAdapter.notifyDataSetChanged()
            try{
                if (results != null) {
                    if(results.isNotEmpty()){
                        //Log.e(TAG, "resultados = " + (results?.get(0)?.categories?.get(0)?.label ?: "--")/*String.format("%", )*/)
                        setarTexto((results?.get(0)?.categories?.get(0)?.label ?: "--"), (results?.get(0)?.categories?.get(0)?.score ?: 0.0f))
                    }
                }
            }catch (e: IndexOutOfBoundsException){
                e.message?.let { Log.e(TAG, "AIAIAI: " + it) }
            } finally {
                // optional finally block
            }
            results?.let { it ->
                if (it.isNotEmpty()) {
                    val sortedCategories = it[0].categories.sortedBy { it?.label }
                    val min = min(sortedCategories.size, categories.size)
                    for (i in 0 until min) {
                        categories[i] = sortedCategories[i]
                    }
                }
            }
            if(categories.size > 0)
                categories[0].let { category ->
                    setarTexto(category?.label, category?.score)
                }
            //viewBinding.textView3.text = categories.get()?.label
            //String.format("%d ms", inferenceTime).also { viewBinding.textView3.text = it }
        }
    }

    private fun setarTexto(label: String?, score: Float?){
        viewBinding.textViewResults.text = String.format("%s (%.2f)", label, score)
        this.score = score!!
        this.label = label!!

    }
}