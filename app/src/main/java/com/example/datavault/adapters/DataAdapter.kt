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
import com.example.datavault.models.DataModelRetrieve
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DataAdapter(
    private var listOfDataModel: MutableList<DataModelRetrieve>,
    private var uidMapOfDataModel: HashMap<String, Int>
) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataAppName: TextView = itemView.findViewById(R.id.tvDataAppName)
        val tvDataUserName: TextView = itemView.findViewById(R.id.tvDataUserName)
        val tvDataEmail: TextView = itemView.findViewById(R.id.tvDataEmail)
        val tvDocId: TextView = itemView.findViewById(R.id.tvDocId)
        val ivDataEditIcon: ImageView = itemView.findViewById(R.id.ivDataEditIcon)
        val ivDataDeleteIcon: ImageView = itemView.findViewById(R.id.ivDataDeleteIcon)
        val ivDataFavoriteIcon: ImageView = itemView.findViewById(R.id.ivDataFavoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_container, parent, false)
        return DataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val currentData = listOfDataModel[position]
        updateContentOfView(holder, currentData)
    }

    override fun getItemCount(): Int {
        return listOfDataModel.size
    }

    private fun updateContentOfView(holder: DataViewHolder, currentData: DataModelRetrieve) {

        holder.apply {
            tvDataAppName.text = currentData.appName
            tvDataUserName.text = currentData.userName
            tvDataEmail.text = currentData.email
            tvDocId.text = currentData.docId
            ivDataFavoriteIcon.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context, "Added to favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ivDataEditIcon.setOnClickListener {
                val intent = Intent(itemView.context, EditDataActivity::class.java)
                intent.putExtra("fireStoreDocId", currentData.fireStoreDocId)
                itemView.context.startActivity(intent)
            }
            ivDataDeleteIcon.setOnClickListener {
                val indexPos: Int = uidMapOfDataModel[tvDocId.text.toString()]!!
                val dataContent: DataModelRetrieve = listOfDataModel[indexPos]
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