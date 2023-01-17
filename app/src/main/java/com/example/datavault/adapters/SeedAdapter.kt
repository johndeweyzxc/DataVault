package com.example.datavault.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.EditDataActivity
import com.example.datavault.R
import com.example.datavault.schema.SeedSchema
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SeedAdapter(
    private var listOfDataModel: MutableList<SeedSchema>,
    private var uidMapOfDataModel: HashMap<String, Int>,
    private val maxCardElevation: Float = 8.0F
) : RecyclerView.Adapter<SeedAdapter.SeedViewHolder>() {

    class SeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardViewSeed: MaterialCardView = itemView.findViewById(R.id.cardViewSeed)
        val tvSeedAppName: TextView = itemView.findViewById(R.id.tvSeedAppName)
        val tvSeedUserName: TextView = itemView.findViewById(R.id.tvSeedUserName)
        val tvSeedEmail: TextView = itemView.findViewById(R.id.tvSeedEmail)
        val tvDocId: TextView = itemView.findViewById(R.id.tvDocId)
        val ivSeedEditIcon: ImageView = itemView.findViewById(R.id.ivSeedEditIcon)
        val ivSeedDeleteIcon: ImageView = itemView.findViewById(R.id.ivSeedDeleteIcon)
        val ivSeedFavoriteIcon: ImageView = itemView.findViewById(R.id.ivSeedFavoriteIcon)
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
            cardViewSeed.cardElevation = maxCardElevation
            tvSeedAppName.text = currentData.appName
            tvSeedUserName.text = currentData.userName
            tvSeedEmail.text = currentData.email
            tvDocId.text = currentData.docId
            ivSeedFavoriteIcon.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context, "Added to favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ivSeedEditIcon.setOnClickListener {
                val intent = Intent(itemView.context, EditDataActivity::class.java)
                intent.putExtra("fireStoreDocId", currentData.fireStoreDocId)
                itemView.context.startActivity(intent)
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

    private fun fireStoreDeleteData(itemView: View, userId: String, firestoreDocId: String) {
        Firebase.firestore.collection("generatedUserData")
            .document(userId)
            .collection("data")
            .document(firestoreDocId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    itemView.context, "Successfully deleted the data",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    itemView.context, "Failed to delete data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}