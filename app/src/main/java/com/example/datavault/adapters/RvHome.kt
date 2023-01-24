package com.example.datavault.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.R
import com.example.datavault.schema.SeedSchema
import com.example.datavault.views.EditSeedDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

// Adapter for recycler view in HomeFragment to render list of document.
class RvHome(
    private val listOfDataModel: MutableList<SeedSchema>,
    private val uidHashMap: HashMap<String, Int>,
    private val fragmentManager: FragmentManager,
    private val clipBoard: ClipboardManager,
) : RecyclerView.Adapter<RvHome.SeedViewHolder>() {

    class SeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewSeed: MaterialCardView = itemView.findViewById(R.id.cardViewSeed)
        val tvSeedAppName: TextView = itemView.findViewById(R.id.tvSeedAppName)
        val tvDocId: TextView = itemView.findViewById(R.id.tvDocId)
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
        val currentData = listOfDataModel[position]
        updateContentOfView(holder, currentData)
    }

    override fun getItemCount(): Int {
        return listOfDataModel.size
    }

    private fun updateContentOfView(holder: SeedViewHolder, currentData: SeedSchema) {

        holder.apply {
            val itemV = holder.itemView

            // Set the animation for the card view when it appears on the screen
            cardViewSeed.startAnimation(AnimationUtils.loadAnimation(itemV.context, R.anim.rvhome_item))

            cardViewSeed.setOnClickListener {
                val totalFragments = fragmentManager.backStackEntryCount
                if (totalFragments > 0) {
                    // Prevent adding duplicate fragment to the stack when the user clicks twice on the card
                    val latestFragment = fragmentManager.getBackStackEntryAt(totalFragments - 1)
                    if (latestFragment.name == "EditSeedDialog") {
                        fragmentManager.popBackStack()
                    }
                }

                fragmentManager.beginTransaction().apply {
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    add(R.id.frameLayoutActivityMain, EditSeedDialog(
                        itemView, currentData.fireStoreDocId, listOfDataModel, uidHashMap
                    ))
                    addToBackStack("EditSeedDialog")
                    commit()
                }
            }

            cardViewSeed.setOnLongClickListener {
                cardViewSeed.isChecked = !cardViewSeed.isChecked
                true
            }

            setTextContent(seedChipUsernameInfo, currentData.userName)
            // When user touch on chips, it copies its content on the clipboard
            copyFromChip(itemV, "Copied username", seedChipUsernameInfo.text.toString(), seedChipUsernameInfo)

            setTextContent(seedChipEmailInfo, currentData.email)
            copyFromChip(itemV, "Copied email", seedChipEmailInfo.text.toString(), seedChipEmailInfo)

            setTextContent(seedChipPhoneNumberInfo, currentData.phoneNumber)
            copyFromChip(itemV, "Copied phone number", seedChipPhoneNumberInfo.text.toString(), seedChipPhoneNumberInfo)

            setTextContent(seedChipPasswordInfo, currentData.password)
            copyFromChip(itemV, "Copied password", seedChipPasswordInfo.text.toString(), seedChipPasswordInfo)

            tvSeedAppName.text = currentData.appName
            tvDocId.text = currentData.docId
        }
    }

    private fun setTextContent(textView: TextView, text: String) {
        if (text.length > 8) {
            val data = "${text.take(4)}...${text.takeLast(4)}"
            textView.text = data
        } else {
            textView.text = text
        }
    }

    private fun copyFromChip(itemView: View, label: String, text: String, chip: Chip) {
        chip.setOnClickListener {
            val clip = ClipData.newPlainText(label, text)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(itemView.context, label, Toast.LENGTH_SHORT).show()
        }
    }
}
