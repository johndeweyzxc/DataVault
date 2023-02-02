package com.example.datavault.schema

import com.google.firebase.Timestamp

data class UserProfile (
    var name: String,
    var updateAt: Timestamp?,
    var createdAt: Timestamp?
)