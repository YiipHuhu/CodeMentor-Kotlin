package com.example.codementor.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.codementor.MainActivity
import com.example.codementor.R
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val keepConnected = sharedPreferences.getBoolean("keepConnected", false)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (keepConnected && currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.etEmailLogin)
        val passwordField = findViewById<EditText>(R.id.etPasswordLogin)
        val keepConnectedSwitch = findViewById<SwitchCompat>(R.id.switchKeepConnected)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerTextView = findViewById<TextView>(R.id.LoginTextView)

        // Inicia estado do Switch
        keepConnectedSwitch.isChecked = keepConnected
        updateSwitchTrackColor(keepConnectedSwitch)

        // Adiciona listener para alterar cor do Switch dinamicamente
        keepConnectedSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchTrackColor(keepConnectedSwitch)
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                        sharedPreferences.edit()
                            .putBoolean("keepConnected", keepConnectedSwitch.isChecked).apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        val exception = task.exception
                        when {
                            exception is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Senha incorreta. Tente novamente.", Toast.LENGTH_SHORT).show()
                            }

                            exception is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, "Conta não encontrada. Verifique o email.", Toast.LENGTH_SHORT).show()
                            }

                            exception is FirebaseTooManyRequestsException -> {
                                Toast.makeText(this, "Muitas tentativas falhas. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Formato de email incorreto!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun updateSwitchTrackColor(switch: SwitchCompat) {
        val colorResId = if (switch.isChecked) {
            R.color.switch_track_color_active // Branco para ativado
        } else {
            R.color.switch_track_color // Cinza para desativado
        }
        switch.trackTintList = ContextCompat.getColorStateList(this, colorResId)
    }
}
