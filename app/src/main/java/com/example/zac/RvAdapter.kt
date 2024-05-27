package com.example.zac

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zac.databinding.ItemLayoutBinding
import com.example.zac.modules.Homeworld
import com.example.zac.modules.StarWarsCharacter
import com.example.zac.utils.RetrofitInstance
import kotlinx.coroutines.*
import retrofit2.Response

// Adapter class for the RecyclerView
class RvAdapter(private val characterList: List<StarWarsCharacter>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    // ViewHolder inner class to hold references to views for each item
    inner class ViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return characterList.size
    }

    // Called to display data at a specific position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = characterList[position]
        holder.binding.apply {
            tvCharacterName.text = currentItem.name
            tvVehicles.text = "Vehicles: " + currentItem.vehicles.joinToString(", ")
            //tvLanguage.text = currentItem.species.joinToString(", ")

            // Load the species image using Glide
            if (currentItem.species.isNotEmpty()) {
                val speciesImage = currentItem.species[0]
                if (speciesImage != "placeholder") {
                    // Use Glide to load the species image
                    Glide.with(holder.itemView.context)
                        .load(speciesImage) // Use the first image URL from the list
                        .apply(RequestOptions().placeholder(R.drawable.placeholder).error(R.drawable.error))
                        .into(languageImageView)
                } else {
                    languageImageView.setImageResource(R.drawable.placeholder)
                }
            } else {
                languageImageView.setImageResource(R.drawable.placeholder)
            }

            // Set click listener to show character details dialog
            root.setOnClickListener {
                showCharacterDetailsDialog(holder.itemView.context, currentItem)
            }
        }
    }

    // Method to show a dialog with character details
    private fun showCharacterDetailsDialog(context: Context, character: StarWarsCharacter) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.character_details_dialog, null)
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(character.name)
        val dialog = dialogBuilder.create()

        // Bind basic data to dialog views
        dialogView.findViewById<TextView>(R.id.tvCharacterNameDialog).text = "Name: ${character.name}"
        dialogView.findViewById<TextView>(R.id.tvGender).text = "Gender: ${character.gender}"
        dialogView.findViewById<TextView>(R.id.tvSkinColor).text = "Skin Color: ${character.skin_color}"
        val homeworldTextView = dialogView.findViewById<TextView>(R.id.tvHomeTown)
        val filmsTextView = dialogView.findViewById<TextView>(R.id.tvFilmsList)

        // Placeholder for homeworld and films until fetched
        homeworldTextView.text = "Hometown: Loading..."
        filmsTextView.text = "Films: Loading..."

        // Star button
        val starButton = dialogView.findViewById<Button>(R.id.starButton)
        starButton.isSelected = character.isFavorite // Set star state based on character's favorite status

        // Handle star button press
        starButton.setOnClickListener {
            character.isFavorite = !character.isFavorite // Toggle favorite status
            starButton.isSelected = character.isFavorite // Update star state
            // You can handle saving the favorite status to local storage or any other appropriate action here
        }

        // Show dialog
        dialog.show()

        // Fetch homeworld data asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch homeworld data
                val homeworldResponse = RetrofitInstance.api.getHomeworld(character.homeworld)
                val homeworldName = if (homeworldResponse.isSuccessful) {
                    homeworldResponse.body()?.name ?: "Unknown"
                } else {
                    "Unknown"
                }
                homeworldTextView.text = "Hometown: $homeworldName"

                // Fetch films data
                val filmsNames = mutableListOf<String>()
                character.films.forEach { filmUrl ->
                    val filmResponse = RetrofitInstance.api.getFilm(filmUrl)
                    if (filmResponse.isSuccessful) {
                        val film = filmResponse.body()
                        filmsNames.add(film?.title ?: "Unknown")
                    }
                }
                filmsTextView.text = "Films: ${filmsNames.joinToString(", ")}"
            } catch (e: Exception) {
                Log.e("RvAdapter", "Error fetching character details", e)
                homeworldTextView.text = "Hometown: Error"
                filmsTextView.text = "Films: Error"
            }
        }
    }
}
