package com.example.datavault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.example.datavault.schema.SeedSchema
import com.example.datavault.views.EditSeedDialog
import com.google.android.material.card.MaterialCardView

class RvFavorite(private val mainActivity: MainActivity) : RecyclerView.Adapter<RvFavorite.FavoriteViewHolder>(), Database {

    private val listOfFavorites: List<SeedSchema> = mainActivity.viewModel.getListFavorites()

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
                    addToFavorites(itemView.context, currentData.fireStoreDocId, false)
                } else {
                    addToFavorites(itemView.context, currentData.fireStoreDocId, true)
                }
            }

            favoriteCardView.setOnClickListener {
                mainActivity.viewModel.setEditSeedCurrentData(currentData)

                val totalFragments = mainActivity.supportFragmentManager.backStackEntryCount
                if (totalFragments > 0) {
                    // Prevent adding duplicate fragment to the stack when the user clicks twice on the card
                    val latestFragment = mainActivity.supportFragmentManager.getBackStackEntryAt(totalFragments - 1)
                    if (latestFragment.name == "EditSeedDialog") {
                        mainActivity.supportFragmentManager.popBackStack()
                    }
                }

                mainActivity.supportFragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    add(R.id.frameLayoutActivityMain, EditSeedDialog())
                    addToBackStack("EditSeedDialog")
                    commit()
                }
            }
        }
    }
}