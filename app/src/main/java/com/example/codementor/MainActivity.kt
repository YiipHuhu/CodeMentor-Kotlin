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
import java.io.File

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
                    val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
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

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        headerNickname.text = sharedPreferences.getString("nickname", "Bem-vindo!")
        val photoUri = sharedPreferences.getString("photoUri", null)

        if (!photoUri.isNullOrEmpty()) {
            val uri = Uri.parse(photoUri)
            val file = File(uri.path ?: "")

            if (file.exists()) {
                headerImage.setImageURI(uri)
            } else {
                headerImage.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            headerImage.setImageResource(R.drawable.ic_default_profile)
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
