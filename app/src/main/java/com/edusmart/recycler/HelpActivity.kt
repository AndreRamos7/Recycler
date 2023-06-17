package com.edusmart.recycler

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class HelpActivity : AppCompatActivity() {
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = this.getSharedPreferences("RecyclerDados", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_help)

        val textView = findViewById<TextView>(R.id.textViewHelp)
        val button_zerar = findViewById<Button>(R.id.button_zerar)

        textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this.getString(R.string.txt_help), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(this.getString(R.string.txt_help))
        }

        button_zerar.setOnClickListener(View.OnClickListener {
            val editor = sharedPreferences?.edit()
            editor?.putInt("erros", 0)
            editor?.putInt("acertos", 0)
            editor?.apply()
            Toast.makeText(this, "Placar zerado com sucesso.", Toast.LENGTH_LONG).show()
        })
    }
}