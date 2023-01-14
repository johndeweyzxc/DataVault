package com.example.datavault

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DataAdapter : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    lateinit var listOfDataModel: MutableList<DataModel>
    lateinit var uidMapOfDataModel: HashMap<String, Int>
    private val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

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

    private fun updateContentOfView(holder: DataViewHolder, currentData: DataModel) {
        holder.apply {
            tvDataAppName.text = currentData.appName
            tvDataUserName.text = currentData.userName
            tvDataEmail.text = currentData.email
            tvDocId.text = currentData.docId
            ivDataEditIcon.setOnClickListener {
                val dataContent: DataModel = listOfDataModel[uidMapOfDataModel[tvDocId.text.toString()]!!]
                val intent = Intent(itemView.context, EditDataActivity::class.java)
                intent.putExtra("fireStoreDocId", dataContent.fireStoreDocId)
                intent.putExtra("appName", dataContent.appName)
                intent.putExtra("userName", dataContent.userName)
                intent.putExtra("email", dataContent.email)
                intent.putExtra("password", dataContent.password)
                intent.putExtra("phoneNumber", dataContent.phoneNumber)
                itemView.context.startActivity(intent)
            }
            ivDataFavoriteIcon.setOnClickListener {
                Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()

            }
            ivDataDeleteIcon.setOnClickListener {
                val dataContent: DataModel = listOfDataModel[uidMapOfDataModel[tvDocId.text.toString()]!!]
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("Deleting ${dataContent.appName}")
                    .setMessage("You are about to delete ${dataContent.appName}, deleting this is irreversible. " +
                            "Are you sure you want to delete?")
                    .setNeutralButton("CANCEL") { dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton("DELETE") {_, _ ->
                        Firebase.firestore.collection("generatedUserData").document(firebaseUser!!.uid)
                            .collection("data").document(dataContent.fireStoreDocId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(itemView.context, "Successfully deleted the data", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(itemView.context, "Failed to delete the data", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                    .show()
            }
        }
    }

    fun onAddData(dataItem: DataModel, docId: String) {
        listOfDataModel.add(dataItem)
        // Use docId as the key
        uidMapOfDataModel[docId] = itemCount - 1
        notifyItemInserted(itemCount - 1)
    }

    fun onDeleteData(docId: String) {
        val dataIndex: Int? = uidMapOfDataModel[docId]
        if (dataIndex != null) {
            if (uidMapOfDataModel.containsKey(docId)) {
                uidMapOfDataModel.remove(docId)
            }

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount > dataIndex) {
                listOfDataModel.removeAt(dataIndex)
                notifyItemRemoved(dataIndex)
            }
        }
    }

    fun onModifyData(updatedItem: DataModel, docId: String) {
        val dataIndex: Int? = uidMapOfDataModel[docId]
        if (dataIndex != null) {

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount > dataIndex) {
                listOfDataModel[dataIndex] = updatedItem
                notifyItemChanged(dataIndex)
            }
        }
    }
}