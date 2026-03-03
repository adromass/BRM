package com.brm.app

import android.graphics.Bitmap

data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val address: String,
    val rating: Float,
    var bitmap: Bitmap? = null
)