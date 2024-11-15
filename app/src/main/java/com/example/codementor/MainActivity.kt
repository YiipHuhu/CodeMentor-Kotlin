package com.example.codementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.codementor.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configuração das opções de navegação no menu lateral
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
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
                    val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
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
        val headerEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        val headerImage = headerView.findViewById<ImageView>(R.id.imageView)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        headerNickname.text = sharedPreferences.getString("nickname", "Bem-vindo!")
        headerEmail.text = sharedPreferences.getString("email", "email@example.com")
        val photoUri = sharedPreferences.getString("photoUri", null)

        if (!photoUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(photoUri)
                headerImage.setImageURI(uri)
                headerImage.post {
                    try {
                        contentResolver.openInputStream(uri)?.use {
                            Log.d("UpdateHeader", "Photo loaded successfully: $photoUri")
                        }
                    } catch (e: SecurityException) {
                        Log.e("UpdateHeader", "SecurityException: ${e.message}")
                        headerImage.setImageResource(R.drawable.default_profile_image)
                    } catch (e: Exception) {
                        Log.e("UpdateHeader", "Error loading photo: ${e.message}")
                        headerImage.setImageResource(R.drawable.default_profile_image)
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateHeader", "Failed to set photo: ${e.message}")
                headerImage.setImageResource(R.drawable.default_profile_image)
            }
        } else {
            headerImage.setImageResource(R.drawable.default_profile_image)
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeader(binding.navView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
