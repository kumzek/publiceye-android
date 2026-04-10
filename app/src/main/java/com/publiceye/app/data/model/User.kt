package com.publiceye.app.data.model

import com.google.firebase.Timestamp

data class User(
    val uid         : String    = "",
    val displayName : String    = "",
    val email       : String    = "",
    val photoUrl    : String    = "",
    val reportCount : Int       = 0,
    val createdAt   : Timestamp = Timestamp.now(),
)
