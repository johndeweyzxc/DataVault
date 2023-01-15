package com.example.datavault.models

import com.google.firebase.Timestamp

// For retrieving data from the database
data class DataModelRetrieve(
    var appName: String,
    var userName: String,
    var email: String,
    var password: String,
    var phoneNumber: String,
    var docId: String,
    var fireStoreDocId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)