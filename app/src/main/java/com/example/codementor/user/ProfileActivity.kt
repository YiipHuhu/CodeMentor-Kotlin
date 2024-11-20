package com.example.codementor.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.codementor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView) // ID do ImageView na tela de edição
        val changePhotoButton = findViewById<Button>(R.id.btnChangePhoto) // Botão para alterar foto
        val saveButton = findViewById<Button>(R.id.btnSaveProfile) // Botão para salvar alterações
        val nicknameField = findViewById<EditText>(R.id.nicknameEditText)

        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Carregar dados do Firestore
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

            // Salvar dados no Firestore e Storage
            saveUserProfile(userId, nickname)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun loadUserProfile(userId: String, nicknameField: EditText) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname")
                    val photoUrl = document.getString("photoUrl")

                    nicknameField.setText(nickname)

                    if (!photoUrl.isNullOrEmpty()) {
                        // Carregar a foto usando Picasso ou outra biblioteca
                        Picasso.get().load(photoUrl).into(profileImageView)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile(userId: String, nickname: String) {
        // Salvar foto no Firebase Storage (se for selecionada)
        if (imageUri != null) {
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    // Obter URL da imagem salva
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val photoUrl = uri.toString()
                        saveToFirestore(userId, nickname, photoUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Salvar apenas o apelido no Firestore
            saveToFirestore(userId, nickname, null)
        }
    }

    private fun saveToFirestore(userId: String, nickname: String, photoUrl: String?) {
        val userProfile = hashMapOf(
            "nickname" to nickname,
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(userId).set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
