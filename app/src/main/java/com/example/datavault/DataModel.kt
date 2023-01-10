package com.example.datavault

import com.google.firebase.Timestamp

data class DataModel(
    var appName: String,
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var docId: String = "",
    var createdAt: Timestamp?,
    var updatedAt: Timestamp?,
)