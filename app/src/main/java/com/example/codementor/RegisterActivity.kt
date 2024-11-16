package com.example.codementor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailField = findViewById<EditText>(R.id.etEmailRegister)
        val passwordField = findViewById<EditText>(R.id.etPasswordRegister)
        val confirmPasswordField = findViewById<EditText>(R.id.etConfirmPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        registerButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não correspondem!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Recuperar a lista de usuários existentes
            val usersJson = sharedPreferences.getString("users", null)
            val usersMap: MutableMap<String, String> = if (usersJson != null) {
                Gson().fromJson(usersJson, object : TypeToken<MutableMap<String, String>>() {}.type)
            } else {
                mutableMapOf()
            }

            // Verificar se o email já está registrado
            if (usersMap.containsKey(email)) {
                Toast.makeText(this, "Email já registrado!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Adicionar o novo usuário ao mapa
            usersMap[email] = password

            // Salvar o mapa atualizado no SharedPreferences
            val updatedUsersJson = Gson().toJson(usersMap)
            editor.putString("users", updatedUsersJson).apply()

            Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show()

            // Redirecionar para a tela de login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
