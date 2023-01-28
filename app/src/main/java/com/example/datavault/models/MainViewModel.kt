package com.example.datavault.models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.datavault.schema.SeedSchema
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange

// View model for MainActivity
class MainViewModel: ViewModel() {

    private var listDataModel = mutableListOf<SeedSchema>()
    private var listFavorites = mutableListOf<SeedSchema>()
    private var editSeedCurrentData: SeedSchema? = null

    fun countSeed(): Int {
        return listDataModel.size
    }
    fun getListSeed(): MutableList<SeedSchema> {
        return listDataModel
    }
    fun getListFavorites(): MutableList<SeedSchema> {
        return listFavorites
    }
    fun getSeed(fireStoreDocId: String): SeedSchema {
        return listDataModel[linearSearch(fireStoreDocId, listDataModel)]
    }
    fun getEditSeedCurrentData(): SeedSchema? {
        return editSeedCurrentData
    }
    fun setEditSeedCurrentData(currentData: SeedSchema) {
        editSeedCurrentData = currentData
    }

    private fun linearSearch(docId: String, list: List<SeedSchema>): Int {
        var i = -1
        for((index, seed) in list.withIndex()) {
            if (seed.fireStoreDocId == docId) {
                i = index
                break
            }
        }
        return i
    }

    fun convertToModel(changes: DocumentChange): SeedSchema {
        return SeedSchema(
            changes.document.get("appName") as String, changes.document.get("userName") as String,
            changes.document.get("email") as String, changes.document.get("password") as String,
            changes.document.get("phoneNumber") as String, changes.document.get("favorite") as Boolean,
            changes.document.id, changes.document.get("createdAt") as Timestamp?,
            changes.document.get("updatedAt") as Timestamp?,
        )
    }

    fun addData(dataItem: SeedSchema): List<Int> {
        listDataModel.add(dataItem)
        if (dataItem.favorite) {
            listFavorites.add(dataItem)
            Log.i("devlog", "Returning with indexOfFavorites")
            return listOf(countSeed() - 1, listFavorites.size - 1)
        }
        return listOf(countSeed() - 1, -1)
    }

    fun deleteData(deletedItem: SeedSchema): List<Int> {
        val indexOfSeed = linearSearch(deletedItem.fireStoreDocId, listDataModel)
        if (deletedItem.favorite) {
            listDataModel.removeAt(indexOfSeed)
            val indexOfFavorites = linearSearch(deletedItem.fireStoreDocId, listFavorites)
            Log.i("devlog", "Target index of favorites $indexOfFavorites")
            return if (indexOfFavorites != -1) {
                Log.i("devlog", "Present in favorites, deleting existing data")
                listFavorites.removeAt(indexOfFavorites)
                listOf(indexOfSeed, indexOfFavorites)
            } else {
                Log.i("devlog", "Not present in favorites, returning -1")
                listOf(indexOfSeed,  -1)
            }
        }

        listDataModel.removeAt(indexOfSeed)
        return listOf(indexOfSeed, -1)
    }

    fun modifyData(updatedItem: SeedSchema): List<Int> {
        val indexOfSeed = linearSearch(updatedItem.fireStoreDocId, listDataModel)
        val indexOfFavorites = linearSearch(updatedItem.fireStoreDocId, listFavorites)
        listDataModel[indexOfSeed] = updatedItem
        return if (indexOfFavorites == -1) {
            Log.i("devlog", "Not present in favorites, adding data")
            if (updatedItem.favorite) {
                listFavorites.add(updatedItem)
            }
            listOf(indexOfSeed, listFavorites.size - 1)
        } else {
            Log.i("devlog", "Present in favorites, modifying existing data")
            if (updatedItem.favorite) {
                listFavorites[indexOfFavorites] = updatedItem
            } else {
                listFavorites.removeAt(indexOfFavorites)
            }
            listOf(indexOfSeed, indexOfFavorites)
        }
    }
}