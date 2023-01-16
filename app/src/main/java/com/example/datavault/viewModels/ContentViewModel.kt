package com.example.datavault.viewModels

import androidx.lifecycle.ViewModel
import com.example.datavault.schema.SeedSchema

class ContentViewModel: ViewModel() {

    var listDataModel = mutableListOf<SeedSchema>()
    var mapDataModel = HashMap<String, Int>()

    fun addData(dataItem: SeedSchema, docId: String): Int {
        // Cannot add duplicate documents
        if (mapDataModel.containsKey(docId)) {
            return -1
        }
        listDataModel.add(dataItem)
        // Use docId as the key
        mapDataModel[docId] = itemCount() - 1
        return itemCount() - 1
    }

    fun deleteData(docId: String): Int {
        // Document does not exists
        if (!mapDataModel.containsKey(docId)) {
            return -1
        }

        val dataIndex: Int? = mapDataModel[docId]
        if (dataIndex != null) {
            // Remove the key in map when deleting a document.
            if (mapDataModel.containsKey(docId)) {
                mapDataModel.remove(docId)
            }
            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount() > dataIndex) {
                listDataModel.removeAt(dataIndex)
            }
        }
        return dataIndex as Int
    }

    fun modifyData(updatedItem: SeedSchema, docId: String): Int {
        // Document does not exists
        if (!mapDataModel.containsKey(docId)) {
            return -1
        }

        val dataIndex: Int? = mapDataModel[docId]
        if (dataIndex != null) {
            // Check if the index is not null to prevent IndexOutOfBoundsException
            if (itemCount() > dataIndex) {
                listDataModel[dataIndex] = updatedItem
            }
        }
        return dataIndex as Int
    }

    private fun itemCount(): Int {
        return listDataModel.size
    }
}