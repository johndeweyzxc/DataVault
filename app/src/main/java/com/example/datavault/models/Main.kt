package com.example.datavault.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

// View model for MainActivity
class Main: ViewModel() {

    private var listDataModel = mutableListOf<SeedSchema>()
    private var mapDataModel = HashMap<String, Int>()

    fun countItem(): Int {
        return listDataModel.size
    }

    fun getListData(): MutableList<SeedSchema> {
        return listDataModel
    }

    fun getMapData(): HashMap<String, Int> {
        return mapDataModel
    }

    fun getUserEmail(currentUser: FirebaseUser): String {
        return currentUser.email!!
    }

    fun getUserName(currentUser: FirebaseUser): String {
        if (currentUser.displayName.isNullOrBlank()) {
            val createName = currentUser.email.toString().split("@")
            return createName.first()
        }
        return currentUser.displayName!!
    }

    fun getUserphotoUrl(currentUser: FirebaseUser, defaultUrl: String): String {
        if (currentUser.photoUrl == null) {
            return defaultUrl
        }
        return currentUser.photoUrl.toString()
    }

    fun addData(dataItem: SeedSchema, docId: String): Int {
        // Cannot add duplicate documents
        if (mapDataModel.containsKey(docId)) {
            return -1
        }
        listDataModel.add(dataItem)
        // Use docId as the key
        mapDataModel[docId] = listDataModel.size - 1
        return listDataModel.size - 1
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
            if (listDataModel.size > dataIndex) {
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
            if (listDataModel.size > dataIndex) {
                listDataModel[dataIndex] = updatedItem
            }
        }
        return dataIndex as Int
    }

}