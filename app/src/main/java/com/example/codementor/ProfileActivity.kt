package com.example.codementor

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView) // ID do ImageView na tela de edição
        val changePhotoButton = findViewById<Button>(R.id.btnChangePhoto) // Botão para alterar foto
        val saveButton = findViewById<Button>(R.id.btnSaveProfile) // Botão para salvar alterações

        // Carregar foto atual
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val photoUri = sharedPreferences.getString("photoUri", null)
        if (photoUri != null) {
            profileImageView.setImageURI(Uri.parse(photoUri))
        }

        // Abrir galeria para selecionar imagem
        changePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Salvar alterações
        saveButton.setOnClickListener {
            // Salvar outras informações do perfil, como apelido
            val nicknameField = findViewById<EditText>(R.id.nicknameEditText)
            val nickname = nicknameField.text.toString()

            sharedPreferences.edit()
                .putString("nickname", nickname)
                .apply()

            finish() // Retornar para a MainActivity
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                val localImageUri = copyImageToInternalStorage(imageUri)

                // Atualizar ImageView com a nova imagem
                profileImageView.setImageURI(localImageUri)

                // Salvar URI da imagem no SharedPreferences
                val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("photoUri", localImageUri.toString())
                    .apply()
            }
        }
    }

    private fun copyImageToInternalStorage(imageUri: Uri): Uri? {
        return try {
            val resolver: ContentResolver = contentResolver
            val inputStream: InputStream? = resolver.openInputStream(imageUri)

            // Criar arquivo no diretório interno
            val directory = getDir("profile_images", Context.MODE_PRIVATE)
            val file = File(directory, "profile_image.jpg")

            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Erro ao copiar imagem: ${e.message}")
            null
        }
    }
}
