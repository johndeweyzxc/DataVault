package com.example.datavault.models

import com.google.firebase.Timestamp

// For uploading data to the database
data class DataModelWrite(
    var appName: String,
    var userName: String,
    var email: String,
    var password: String,
    var phoneNumber: String,
    var docId: String,
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)