package com.example.datavault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.MainActivity
import com.example.datavault.R
import com.example.datavault.schema.SeedSchema
import com.google.android.material.card.MaterialCardView

class RvFavorite(
    private val listOfFavorites: MutableList<SeedSchema>,
    private val activity: MainActivity
    ) : RecyclerView.Adapter<RvFavorite.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val favoriteCardView: MaterialCardView = itemView.findViewById(R.id.favoriteCardView)
        val favoriteAppName: TextView = itemView.findViewById(R.id.tvFavoriteAppName)
        val ivFavoriteIcon: ImageView = itemView.findViewById(R.id.ivFavoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.favorite_container, parent, false)
        return FavoriteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentData = listOfFavorites[position]
        updateContentOfView(holder, currentData)
    }

    override fun getItemCount(): Int {
        return listOfFavorites.size
    }

    private fun updateContentOfView(holder: FavoriteViewHolder, currentData: SeedSchema) {
        holder.apply {
            // Set the animation for the card view when it appears on the screen
            favoriteCardView.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.rvhome_item))
            favoriteAppName.text = currentData.appName

            ivFavoriteIcon.setOnClickListener {
                if (currentData.favorite) {
                    activity.EditSeed().addToFavorites(itemView.context, currentData.fireStoreDocId, false)
                } else {
                    activity.EditSeed().addToFavorites(itemView.context, currentData.fireStoreDocId, true)
                }
            }
        }
    }
}