package com.example.zac

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

class AboutMeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Set the content view to the activity_about_me layout
        setContentView(R.layout.activity_about_me)

        // Handle window insets to apply padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the name and birthdate
        val nameTextView = findViewById<TextView>(R.id.name)
        val birthdateTextView = findViewById<TextView>(R.id.birthdate)
        nameTextView.text = "Name: Eduardo Gonçalves"
        birthdateTextView.text = "Birthdate: 30/05/2000"

        // Setup BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set the selected item to About Me
        bottomNavigationView.selectedItemId = R.id.menu_about_me

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_star_wars_people -> {
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()  // Finish the current activity
                    true
                }
                R.id.menu_about_me -> {
                    // Already on About Me tab, do nothing
                    true
                }
                else -> false
            }
        }
    }
}
