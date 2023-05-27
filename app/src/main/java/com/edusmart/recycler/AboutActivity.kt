package com.edusmart.recycler

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val textView = findViewById<TextView>(R.id.textViewAbout)
        textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this.getString(R.string.txt_about), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(this.getString(R.string.txt_about))
        }

    }
}