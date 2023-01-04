package com.example.datavault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange

class DataAdapter (
    private var data: MutableList<DataModel>,
    private var uidHashMap: HashMap<String, Int>
    ) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDataAppName: TextView = itemView.findViewById(R.id.tvDataAppName)
        val tvDataUserName: TextView = itemView.findViewById(R.id.tvDataUserName)
        val tvDataEmail: TextView = itemView.findViewById(R.id.tvDataEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.data_container, parent, false)

        return DataViewHolder(itemView)
    }

    fun queryToModel(queryData: DocumentChange): DataModel {
        val appName = queryData.document.get("appName")
        val userName = queryData.document.get("userName")
        val email = queryData.document.get("email")
        val password = queryData.document.get("password")
        val phoneNumber = queryData.document.get("phoneNumber")
        val docId = queryData.document.get("docId")

        return DataModel(
            appName as String,
            userName as String,
            email as String,
            password as String,
            phoneNumber as String,
            docId as String,
        )
    }

    fun onAddData(dataItem: DataModel, docId: String) {
        data.add(dataItem)
        uidHashMap[docId] = data.indexOf(dataItem)
        notifyItemInserted(data.size - 1)
    }

    fun onDeleteData(docId: String) {
        val dataIndex: Int? = uidHashMap[docId]
        if (dataIndex != null) {
            data.removeAt(dataIndex)
            uidHashMap.remove(docId)
            notifyItemRemoved(dataIndex)
        }
    }

    fun onModifyData(updatedItem: DataModel, docId: String) {
        val dataIndex: Int? = uidHashMap[docId]
        if (dataIndex != null) {
            data[dataIndex] = updatedItem
            notifyItemChanged(dataIndex)
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

        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}