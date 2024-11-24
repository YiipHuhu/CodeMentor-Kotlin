package com.example.codementor.user

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codementor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageUri: Uri? = null // Armazenar a imagem temporariamente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        profileImageView = findViewById(R.id.profileImageView)
        val changePhotoButton = findViewById<Button>(R.id.btnChangePhoto)
        val saveButton = findViewById<Button>(R.id.btnSaveProfile)
        val nicknameField = findViewById<EditText>(R.id.nicknameEditText)

        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Carregar dados do perfil
        loadUserProfile(userId, nicknameField)

        // Abrir galeria para selecionar imagem
        changePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Salvar alterações
        saveButton.setOnClickListener {
            val nickname = nicknameField.text.toString().trim()
            if (nickname.isEmpty()) {
                Toast.makeText(this, "Digite um apelido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Salvar apelido no Firestore
            saveUserProfile(userId, nickname)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                // Exibir a imagem selecionada na tela
                profileImageView.setImageURI(selectedImageUri)
            }
        }
    }

    private fun loadUserProfile(userId: String, nicknameField: EditText) {
        // Carregar apelido do Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname")
                    nicknameField.setText(nickname)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Carregar imagem do armazenamento interno
        val imagePath = sharedPreferences.getString("profileImagePath", null)
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            profileImageView.setImageBitmap(bitmap)
        }
    }

    private fun saveUserProfile(userId: String, nickname: String) {
        // Salvar o apelido no Firestore
        firestore.collection("users").document(userId).set(mapOf("nickname" to nickname))
            .addOnSuccessListener {
                // Salvar a imagem localmente, se houver uma imagem selecionada
                if (selectedImageUri != null) {
                    val savedPath = saveImageToInternalStorage(selectedImageUri!!)
                    if (savedPath != null) {
                        sharedPreferences.edit()
                            .putString("profileImagePath", savedPath)
                            .apply()
                    }
                }

                Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageToInternalStorage(imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Criar um arquivo para salvar a imagem
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)

            // Salvar o bitmap no arquivo
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}