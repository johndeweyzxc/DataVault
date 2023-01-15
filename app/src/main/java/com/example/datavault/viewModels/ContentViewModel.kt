package com.example.datavault.viewModels

import androidx.lifecycle.ViewModel
import com.example.datavault.models.DataModelRetrieve

class ContentViewModel: ViewModel() {

    var listDataModel = mutableListOf<DataModelRetrieve>()
    var mapDataModel = HashMap<String, Int>()

    fun addData(dataItem: DataModelRetrieve, docId: String): Int {
        if (mapDataModel.containsKey(docId)) {
            return -1
        }

        listDataModel.add(dataItem)
        // Use docId as the key
        mapDataModel[docId] = itemCount() - 1
        // notifyItemInserted(itemCount() - 1)
        return itemCount() - 1
    }

    fun deleteData(docId: String): Int {
        val dataIndex: Int? = mapDataModel[docId]
        if (dataIndex != null) {
            if (mapDataModel.containsKey(docId)) {
                mapDataModel.remove(docId)
            }

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount() > dataIndex) {
                listDataModel.removeAt(dataIndex)
                // notifyItemRemoved(dataIndex)
            }
        }
        return dataIndex as Int
    }

    fun modifyData(updatedItem: DataModelRetrieve, docId: String): Int {
        val dataIndex: Int? = mapDataModel[docId]
        if (dataIndex != null) {

            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount() > dataIndex) {
                listDataModel[dataIndex] = updatedItem
                // notifyItemChanged(dataIndex)
            }
        }
        return dataIndex as Int
    }

    private fun itemCount(): Int {
        return listDataModel.size
    }
}