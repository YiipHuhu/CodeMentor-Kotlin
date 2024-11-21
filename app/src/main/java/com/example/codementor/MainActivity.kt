package com.example.codementor

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.codementor.chat.ChatActivity
import com.example.codementor.databinding.ActivityMainBinding
import com.example.codementor.user.LoginActivity
import com.example.codementor.user.ProfileActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_home)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.nav_host_fragment_content_main, HomeFragment())
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configuração das opções de navegação no menu lateral
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Configurar itens personalizados do menu lateral
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Abrir a tela de perfil
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_logout -> {
                    // Realizar logout
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                R.id.nav_chat -> {
                    //chat
                    startActivity(Intent(this, ChatActivity::class.java))
                }
                R.id.nav_about -> {
                    //telinha de sobre
                    startActivity(Intent(this, AboutActivity::class.java))
                }
                else -> {
                    // Caso contrário, deixar a navegação padrão lidar com o item
                    navController.navigate(menuItem.itemId)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Atualizar cabeçalho na inicialização
        updateHeader(navView)
    }

    private fun updateHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val headerNickname = headerView.findViewById<TextView>(R.id.nav_header_title)
        val headerImage = headerView.findViewById<ImageView>(R.id.imageView)

        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val firestore = FirebaseFirestore.getInstance()

        if (userId != null) {
            // pega o nickname do user no Firestore
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nickname = document.getString("nickname") ?: "Bem-vindo!"
                        headerNickname.text = nickname
                    }
                }
                .addOnFailureListener { e ->
                    // caso de errado, usa o padrão "Bem-vindo!"
                    headerNickname.text = "Bem-vindo!"
                }
        } else {
            // Se o usuário não definido o nome
            headerNickname.text = "Bem-vindo ao CodeMentor"
        }

        // Obter caminho da imagem salva no SharedPreferences
        val imagePath = sharedPreferences.getString("profileImagePath", null)

        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)

            // Verifica se o arquivo da imagem existe antes de carregá-lo
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                headerImage.setImageBitmap(bitmap)
            } else {
                headerImage.setImageResource(R.drawable.ic_default_profile) // Imagem padrão
            }
        } else {
            headerImage.setImageResource(R.drawable.ic_default_profile) // Imagem padrão
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeader(binding.navView)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
