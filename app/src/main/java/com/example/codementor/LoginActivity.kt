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

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
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

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            val usersJson = sharedPreferences.getString("users", null)
            val usersMap: Map<String, String> = if (usersJson != null) {
                Gson().fromJson(usersJson, object : TypeToken<Map<String, String>>() {}.type)
            } else {
                mapOf()
            }

            if (usersMap[email] == password) {
                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                if (keepConnectedSwitch.isChecked) {
                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                }

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Credenciais inválidas!", Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
