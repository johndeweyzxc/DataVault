package com.example.datavault

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange

class DataAdapter (
    private var data: MutableList<DataModel>,
    private var uidHashMap: HashMap<String, Int>
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

    fun queryToModel(queryData: DocumentChange): DataModel {
        val appName = queryData.document.get("appName")
        val userName = queryData.document.get("userName")
        val email = queryData.document.get("email")
        val password = queryData.document.get("password")
        val phoneNumber = queryData.document.get("phoneNumber")
        val docId = queryData.document.get("docId")
        val fireStoreDocId = queryData.document.id
        val createdAt = queryData.document.get("createdAt")
        val updatedAt = queryData.document.get("updatedAt")

        return DataModel(
            appName as String,
            userName as String,
            email as String,
            password as String,
            phoneNumber as String,
            docId as String,
            fireStoreDocId,
            createdAt as Timestamp?,
            updatedAt as Timestamp?,
        )
    }

    fun onAddData(dataItem: DataModel, docId: String) {
        data.add(dataItem)
        uidHashMap[docId] = itemCount - 1
        notifyItemInserted(itemCount - 1)
    }

    fun onDeleteData(docId: String) {
        val dataIndex: Int? = uidHashMap[docId]
        if (dataIndex != null) {
            if (uidHashMap.containsKey(docId)) {
                uidHashMap.remove(docId)
            }

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount > dataIndex) {
                data.removeAt(dataIndex)
                notifyItemRemoved(dataIndex)
            }
        }
    }

    fun onModifyData(updatedItem: DataModel, docId: String) {
        val dataIndex: Int? = uidHashMap[docId]
        if (dataIndex != null) {

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount > dataIndex) {
                data[dataIndex] = updatedItem
                notifyItemChanged(dataIndex)
            }
        }
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        // Current dataItem
        val currentData = data[position]
        // Apply text to each views
        holder.apply {
            tvDataAppName.text = currentData.appName
            tvDataUserName.text = currentData.userName
            tvDataEmail.text = currentData.email
            tvDocId.text = currentData.docId
            ivDataEditIcon.setOnClickListener {
                val dataContent: DataModel = data[uidHashMap[tvDocId.text.toString()]!!]
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
                Toast.makeText(holder.itemView.context, "Deleted this data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}