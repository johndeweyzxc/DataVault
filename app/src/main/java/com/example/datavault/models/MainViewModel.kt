package com.example.datavault.models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.datavault.schema.SeedSchema
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange

// View model for MainActivity
class MainViewModel: ViewModel() {

    private var listDataModel = mutableListOf<SeedSchema>()
    private var listFavorites = mutableListOf<SeedSchema>()
    private var editSeedCurrentData: SeedSchema? = null

    var userProfileExists = MutableLiveData<Boolean>()
    var userName = MutableLiveData<String>()
    var userEmail =  MutableLiveData<String>()

    var currentUserProfileImage: Bitmap? = null

    // Returns -1 if it does not found the item in the list
    // This binary search is only use when retrieving data
    private fun binarySearch(list: List<SeedSchema>, keyInt: Int): Int {
        var low = 0
        var high = list.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            when {
                list[mid].indexId < keyInt -> low = mid + 1
                list[mid].indexId > keyInt -> high = mid - 1
                else -> return mid
            }
        }
        return -1
    }

    // Returns -1 if it does not found the item in the list
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

    fun countSeed(): Int {
        return listDataModel.size
    }
    fun getListSeed(): MutableList<SeedSchema> {
        return listDataModel
    }
    fun getListFavorites(): MutableList<SeedSchema> {
        return listFavorites
    }
    fun getSeed(keyInt: Int): SeedSchema {
        return listDataModel[binarySearch(listDataModel, keyInt)]
    }
    fun getEditSeedCurrentData(): SeedSchema? {
        return editSeedCurrentData
    }
    fun setEditSeedCurrentData(currentData: SeedSchema) {
        Log.i("devlog",
            "[setEditSeedCurrentData()] Setting current data for EditSeedDialog -> ${currentData.appName}")
        editSeedCurrentData = currentData
    }

    fun convertToModel(changes: DocumentChange): SeedSchema {
        return SeedSchema(
            changes.document.get("appName") as String, changes.document.get("userName") as String,
            changes.document.get("email") as String, changes.document.get("password") as String,
            changes.document.get("phoneNumber") as String, changes.document.get("favorite") as Boolean,
            changes.document.id, 0, changes.document.get("createdAt") as Timestamp?,
            changes.document.get("updatedAt") as Timestamp?,
        )
    }

    fun addData(dataItem: SeedSchema): List<Int> {
        dataItem.indexId = listDataModel.size - 1
        listDataModel.add(dataItem)
        if (dataItem.favorite) {
            listFavorites.add(dataItem)

            Log.d("devlog",
                "[addData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
            )
            return listOf(countSeed() - 1, listFavorites.size - 1)
        }
        Log.d("devlog",
            "[addData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
        )
        return listOf(countSeed() - 1, -1)
    }

    fun deleteData(deletedItem: SeedSchema): List<Int> {
        val indexOfSeed = linearSearch(deletedItem.fireStoreDocId, listDataModel)
        if (deletedItem.favorite) {
            listDataModel.removeAt(indexOfSeed)
            val indexOfFavorites = linearSearch(deletedItem.fireStoreDocId, listFavorites)

            return if (indexOfFavorites != -1) {
                listFavorites.removeAt(indexOfFavorites)

                Log.d("devlog",
                    "[deleteData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
                )
                listOf(indexOfSeed, indexOfFavorites)
            } else {
                Log.d("devlog",
                    "[deleteData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
                )
                listOf(indexOfSeed,  -1)
            }
        }

        listDataModel.removeAt(indexOfSeed)
        Log.d("devlog",
            "[deleteData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
        )
        return listOf(indexOfSeed, -1)
    }

    fun modifyData(updatedItem: SeedSchema): List<Int> {
        val indexOfSeed = linearSearch(updatedItem.fireStoreDocId, listDataModel)
        val indexOfFavorites = linearSearch(updatedItem.fireStoreDocId, listFavorites)
        listDataModel[indexOfSeed] = updatedItem

        return if (indexOfFavorites == -1) {
            if (updatedItem.favorite) {
                listFavorites.add(updatedItem)
            }

            Log.d("devlog",
                "[modifyData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
            )
            listOf(indexOfSeed, listFavorites.size - 1)
        } else {
            if (updatedItem.favorite) {
                listFavorites[indexOfFavorites] = updatedItem
            } else {
                listFavorites.removeAt(indexOfFavorites)
            }

            Log.d("devlog",
                "[modifyData()] Size of seed -> ${countSeed()}, size of favorites -> ${listFavorites.size}"
            )
            listOf(indexOfSeed, indexOfFavorites)
        }
    }
}