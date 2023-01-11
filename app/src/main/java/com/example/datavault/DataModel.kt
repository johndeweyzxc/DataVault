package com.example.datavault

import com.google.firebase.Timestamp

// Uploading data to the database
data class DataModelUpload(
    var appName: String,
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var docId: String = "",
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)

// For retrieving data from the database
data class DataModel(
    var appName: String,
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var docId: String = "",
    var fireStoreDocId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)