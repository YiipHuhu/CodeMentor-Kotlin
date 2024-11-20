package com.example.codementor.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.codementor.R
import com.example.codementor.chat.ChatActivity

class ExplanationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receber o nome do módulo passado pelo Intent
        val moduloName = intent.getStringExtra("moduloName")

        // Definir o layout correspondente baseado no módulo
        val layoutId = when (moduloName) {
            "Introdução a Kotlin" -> R.layout.explanation_1
            "Declaração de variáveis em Kotlin" -> R.layout.explanation_2
            "O que é RecyclerView" -> R.layout.explanation_3
            else -> {
                finish()
                return
            }
        }

        setContentView(layoutId) // Define o layout da tela

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.chatButton).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
