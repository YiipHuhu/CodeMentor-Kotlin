package com.example.codementor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // usaando o sharedpreferences pra ajustar se deve ou nao manter o login
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val keepConnected = sharedPreferences.getBoolean("keepConnected", false)

        // Inicialização do FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Verificar se o usuário já está logado e se escolheu "manter conectado"
        val currentUser = auth.currentUser
        if (keepConnected && currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.etEmailLogin)
        val passwordField = findViewById<EditText>(R.id.etPasswordLogin)
        val keepConnectedSwitch = findViewById<Switch>(R.id.switchKeepConnected)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerTextView = findViewById<TextView>(R.id.tvRegister)

        // inicia estado do Switch
        keepConnectedSwitch.isChecked = keepConnected

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Autenticar o usuário no Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                        // Salvar a preferência de "manter conectado"
                        sharedPreferences.edit()
                            .putBoolean("keepConnected", keepConnectedSwitch.isChecked).apply()

                        // Redirecionar para a tela principal
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Exibir mensagem de erro
                        Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
