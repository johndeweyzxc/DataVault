package com.example.datavault.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.datavault.R
import com.example.datavault.components.DeleteDataDialog
import com.example.datavault.models.DataModelRetrieve

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
                Toast.makeText(
                    holder.itemView.context, "Editing this data",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ivDataDeleteIcon.setOnClickListener {
                val indexPos: Int = uidMapOfDataModel[tvDocId.text.toString()]!!
                val dataContent: DataModelRetrieve = listOfDataModel[indexPos]
                DeleteDataDialog().notifyUser(itemView.context, dataContent)
            }
        }
    }

//    fun onAddData(dataItem: DataModelRetrieve, docId: String) {
//        listOfDataModel.add(dataItem)
//        // Use docId as the key
//        uidMapOfDataModel[docId] = itemCount - 1
//        notifyItemInserted(itemCount - 1)
//    }
//
//    fun onDeleteData(docId: String) {
//        val dataIndex: Int? = uidMapOfDataModel[docId]
//        if (dataIndex != null) {
//            if (uidMapOfDataModel.containsKey(docId)) {
//                uidMapOfDataModel.remove(docId)
//            }
//
//            // Check if the index is not null to prevent IndexOutOfBoundsException
//            if (itemCount > dataIndex) {
//                listOfDataModel.removeAt(dataIndex)
//                notifyItemRemoved(dataIndex)
//            }
//        }
//    }
//
//    fun onModifyData(updatedItem: DataModelRetrieve, docId: String) {
//        val dataIndex: Int? = uidMapOfDataModel[docId]
//        if (dataIndex != null) {
//
//            // Check if the index is not null to prevent IndexOutOfBoundsException
//            if (itemCount > dataIndex) {
//                listOfDataModel[dataIndex] = updatedItem
//                notifyItemChanged(dataIndex)
//            }
//        }
//    }
}