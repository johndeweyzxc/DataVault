package com.example.datavault.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.R
import com.example.datavault.fragments.EditSeedFragment
import com.example.datavault.schema.SeedSchema
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SeedAdapter(
    private var listOfDataModel: MutableList<SeedSchema>,
    private var uidMapOfDataModel: HashMap<String, Int>,
    private val fragmentManger: FragmentManager,
    private val frameLayoutId: Int,
    private val clipBoard: ClipboardManager,
) : RecyclerView.Adapter<SeedAdapter.SeedViewHolder>() {

    class SeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSeedAppName: TextView = itemView.findViewById(R.id.tvSeedAppName)
        val tvDocId: TextView = itemView.findViewById(R.id.tvDocId)
        val ivSeedEditIcon: ImageView = itemView.findViewById(R.id.ivSeedEditIcon)
        val ivSeedDeleteIcon: ImageView = itemView.findViewById(R.id.ivSeedDeleteIcon)
        val ivSeedFavoriteIcon: ImageView = itemView.findViewById(R.id.ivSeedFavoriteIcon)
        val seedChipUsernameInfo: Chip = itemView.findViewById(R.id.seedChipUsernameInfo)
        val seedChipEmailInfo: Chip = itemView.findViewById(R.id.seedChipEmailInfo)
        val seedChipPhoneNumberInfo: Chip = itemView.findViewById(R.id.seedChipPhoneNumberInfo)
        val seedChipPasswordInfo: Chip = itemView.findViewById(R.id.seedChipPasswordInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeedViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.seed_container, parent, false)
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

            seedChipUsernameInfo.text = currentData.userName
            copyFromChip(itemV, "Copied username", seedChipUsernameInfo.text.toString(),
                seedChipUsernameInfo)

            seedChipEmailInfo.text = currentData.email
            copyFromChip(itemV, "Copied email", seedChipEmailInfo.text.toString(), seedChipEmailInfo)

            seedChipPhoneNumberInfo.text = currentData.phoneNumber
            copyFromChip(itemV, "Copied phone number", seedChipPhoneNumberInfo.text.toString(),
                seedChipPhoneNumberInfo)

            seedChipPasswordInfo.text = currentData.password
            copyFromChip(itemV, "Copied phone number", seedChipPasswordInfo.text.toString(),
                seedChipPasswordInfo)

            tvSeedAppName.text = currentData.appName
            tvDocId.text = currentData.docId

            ivSeedFavoriteIcon.setOnClickListener {
                Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT
                ).show()
            }
            ivSeedEditIcon.setOnClickListener {
                fragmentManger.beginTransaction().apply {
                    add(frameLayoutId, EditSeedFragment(currentData.fireStoreDocId))
                    addToBackStack("EditSeedFragment")
                    commit()
                }
            }
            ivSeedDeleteIcon.setOnClickListener {
                val indexPos: Int = uidMapOfDataModel[tvDocId.text.toString()]!!
                val dataContent: SeedSchema = listOfDataModel[indexPos]
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid

                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Deleting ${dataContent.appName}")
                    .setMessage("Do you really want to delete ${dataContent.appName}?")
                    .setNeutralButton("CANCEL") { dialog, _ ->
                        dialog.cancel()
                        return@setNeutralButton
                    }
                    .setPositiveButton("DELETE") {_, _ ->
                        if (userId != null) {
                            fireStoreDeleteData(itemView, userId, dataContent.fireStoreDocId)
                        }
                        return@setPositiveButton
                    }.show()
            }
        }
    }

    private fun copyFromChip(itemView: View, label: String, text: String, chip: Chip) {
        chip.setOnClickListener {
            val clip = ClipData.newPlainText(label, text)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(itemView.context, label, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fireStoreDeleteData(itemView: View, userId: String, firestoreDocId: String) {
        Firebase.firestore.collection("generatedUserData")
            .document(userId)
            .collection("data")
            .document(firestoreDocId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(itemView.context, "Successfully deleted the data", Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(itemView.context, "Failed to delete data", Toast.LENGTH_SHORT
                ).show()
            }
    }
}