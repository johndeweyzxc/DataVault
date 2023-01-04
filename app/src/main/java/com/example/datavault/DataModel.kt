package com.example.datavault

data class DataModel(
    var appName: String,
    var userName: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var docId: String = "",
)