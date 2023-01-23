package com.example.datavault.schema

import com.google.firebase.Timestamp

// For uploading data to the database
data class SeedSchemaUpload(
    var appName: String,
    var userName: String,
    var email: String,
    var password: String,
    var phoneNumber: String,
    var docId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)

// For retrieving data from the database
data class SeedSchema(
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