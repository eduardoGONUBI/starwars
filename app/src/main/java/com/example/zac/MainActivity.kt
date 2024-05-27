package com.example.zac

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zac.databinding.ActivityMainBinding
import com.example.zac.modules.StarWarsCharacter
import com.example.zac.utils.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvAdapter: RvAdapter
    private var charactersList: MutableList<StarWarsCharacter> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if network is available, fetch characters if it is
        if (isNetworkAvailable()) {
            fetchAllCharacters("https://swapi.dev/api/people/")
        } else {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show()
        }

        // Set up bottom navigation
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_about_me -> {
                    // Do nothing if already on About Me tab
                    if (item.isChecked) return@setOnNavigationItemSelectedListener false

                    // Navigate to AboutMeActivity
                    val intent = Intent(this, AboutMeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_star_wars_people -> {
                    // Do nothing if already on Star Wars People tab
                    if (item.isChecked) return@setOnNavigationItemSelectedListener false

                    // Stay in the current activity and refresh the list
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Set the default selected item in the bottom navigation view
        binding.bottomNavigation.selectedItemId = R.id.menu_star_wars_people
    }

    // Function to fetch all characters from the API
    private fun fetchAllCharacters(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getAllCharacters(url)
            } catch (e: IOException) {
                Log.e("MainActivity", "Network error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "App error: Unable to resolve host. Please check your internet connection.", Toast.LENGTH_LONG).show()
                }
                return@launch
            } catch (e: HttpException) {
                Log.e("MainActivity", "HTTP error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "HTTP error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                val characters = response.body()!!.results
                withContext(Dispatchers.Main) {
                    charactersList.addAll(characters)
                    characters.forEach { character ->
                        fetchCharacterVehicles(character)
                        fetchCharacterLanguage(character)
                    }
                    binding.rvMain.apply {
                        rvAdapter = RvAdapter(charactersList)
                        adapter = rvAdapter
                        layoutManager = LinearLayoutManager(this@MainActivity)
                    }
                    response.body()!!.next?.let {
                        fetchAllCharacters(it)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    val errorMessage = "Failed to retrieve characters: ${response.code()} ${response.message()}"
                    Log.e("MainActivity", errorMessage)
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Function to fetch character vehicles
    private fun fetchCharacterVehicles(character: StarWarsCharacter) {
        lifecycleScope.launch(Dispatchers.IO) {
            val vehicleNames = mutableListOf<String>()
            for (vehicleUrl in character.vehicles) {
                val response = try {
                    RetrofitInstance.api.getVehicle(vehicleUrl)
                } catch (e: IOException) {
                    Log.e("MainActivity", "Network error: ${e.message}", e)
                    continue
                } catch (e: HttpException) {
                    Log.e("MainActivity", "HTTP error: ${e.message}", e)
                    continue
                }

                if (response.isSuccessful && response.body() != null) {
                    vehicleNames.add(response.body()!!.name)
                }
            }

            withContext(Dispatchers.Main) {
                character.vehicles = vehicleNames
                rvAdapter.notifyDataSetChanged()
            }
        }
    }

    // Function to fetch character language/species
    private fun fetchCharacterLanguage(character: StarWarsCharacter) {
        lifecycleScope.launch(Dispatchers.IO) {
            val speciesLanguageImages = mutableListOf<String>()
            for (speciesUrl in character.species) {
                val response = try {
                    RetrofitInstance.api.getSpecies(speciesUrl)
                } catch (e: IOException) {
                    Log.e("MainActivity", "Network error: ${e.message}", e)
                    continue
                } catch (e: HttpException) {
                    Log.e("MainActivity", "HTTP error: ${e.message}", e)
                    continue
                }

                if (response.isSuccessful && response.body() != null) {
                    val language = response.body()!!.language
                    val processedLanguage = processLanguage(language)
                    val imageUrl = generateAvatarUrl(processedLanguage)
                    Log.d("MainActivity", "Generated avatar URL: $imageUrl") // Log the generated URL
                    speciesLanguageImages.add(imageUrl)
                }
            }

            withContext(Dispatchers.Main) {
                character.species = speciesLanguageImages
                rvAdapter.notifyDataSetChanged()
            }
        }
    }

    // Function to process language to generate initials for avatar URL
    private fun processLanguage(language: String): String {
        val words = language.split(" ")
        return if (words.size == 1) {
            val word = words[0]
            if (word.length > 1) {
                "${word.first()}${word.last()}"
            } else {
                word
            }
        } else {
            "${words.first().first()}${words.last().first()}"
        }
    }

    // Function to generate avatar URL based on initials
    private fun generateAvatarUrl(initials: String): String {
        // Check if initials are "na" (case-sensitive) and switch them to "ww", otherwise make them lowercase
        val modifiedInitials = if (initials == "na") "ww" else initials.toLowerCase()

        // If initials are "Na", keep them as is
        val finalInitials = if (initials == "Na") "Na" else modifiedInitials

        return if (finalInitials == "ww") {
            // Return placeholder image URL
            "placeholder"
        } else {
            // Encode initials to ensure they are URL safe
            val encodedInitials = java.net.URLEncoder.encode(finalInitials, "UTF-8")
            "https://eu.ui-avatars.com/api/?name=$encodedInitials"
        }
    }

    // Function to check network availability
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
