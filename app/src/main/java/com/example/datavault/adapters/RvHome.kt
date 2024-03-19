package com.example.datavault.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.Database
import com.example.datavault.MainActivity
import com.johndeweydev.datavault.R
import com.example.datavault.schema.SeedSchema
import com.example.datavault.views.EditSeedDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class RvHome(private val mainActivity: MainActivity) : RecyclerView.Adapter<RvHome.SeedViewHolder>(), Database {

    private val listOfData: List<SeedSchema> = mainActivity.viewModel.getListSeed()

    class SeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewSeed: MaterialCardView = itemView.findViewById(R.id.cardViewSeed)
        val tvSeedAppName: TextView = itemView.findViewById(R.id.tvSeedAppName)
        val ivSeedFavorite: ImageView = itemView.findViewById(R.id.ivSeedFavorite)
        val seedChipUsernameInfo: Chip = itemView.findViewById(R.id.seedChipUsernameInfo)
        val seedChipEmailInfo: Chip = itemView.findViewById(R.id.seedChipEmailInfo)
        val seedChipPhoneNumberInfo: Chip = itemView.findViewById(R.id.seedChipPhoneNumberInfo)
        val seedChipPasswordInfo: Chip = itemView.findViewById(R.id.seedChipPasswordInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeedViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.seed_container, parent, false)
        return SeedViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SeedViewHolder, position: Int) {
        val currentData = listOfData[position]
        updateContentOfView(holder, currentData)
    }

    override fun getItemCount(): Int {
        return listOfData.size
    }

    private fun updateContentOfView(holder: SeedViewHolder, currentData: SeedSchema) {

        holder.apply {
            if (currentData.favorite) {
                ivSeedFavorite.setBackgroundResource(R.drawable.ic_seed_fav)
            } else {
                ivSeedFavorite.setBackgroundResource(R.drawable.ic_seed_not_fav)
            }

            ivSeedFavorite.setOnClickListener {
                if (currentData.favorite) {
                    addToFavorites(itemView.context, currentData.fireStoreDocId, false)
                } else {
                    addToFavorites(itemView.context, currentData.fireStoreDocId, true)
                }
            }

            cardViewSeed.setOnClickListener {
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

            // When user touch on chips, it copies its content on the clipboard
            copyFromChip(itemView, "Copied username", currentData.userName, seedChipUsernameInfo)
            setTextContent(seedChipUsernameInfo, currentData.userName)

            copyFromChip(itemView, "Copied email", currentData.email, seedChipEmailInfo)
            setTextContent(seedChipEmailInfo, currentData.email)

            copyFromChip(itemView, "Copied phone number", currentData.phoneNumber, seedChipPhoneNumberInfo)
            setTextContent(seedChipPhoneNumberInfo, currentData.phoneNumber)

            copyFromChip(itemView, "Copied password", currentData.password, seedChipPasswordInfo)
            setTextContent(seedChipPasswordInfo, currentData.password)

            tvSeedAppName.text = currentData.appName
        }
    }

    private fun setTextContent(chip: TextView, text: String) {
        if (text.length > 8) {
            val hiddenData = "${text.take(4)}...${text.takeLast(4)}"
            chip.text = hiddenData
        } else {
            chip.text = text
        }
    }

    private fun copyFromChip(itemView: View, label: String, data: String, chip: Chip) {
        chip.setOnClickListener {
            val clip = ClipData.newPlainText(label, data)
            val clipBoardManager = mainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipBoardManager.setPrimaryClip(clip)
            Toast.makeText(itemView.context, label, Toast.LENGTH_SHORT).show()
        }
    }
}
