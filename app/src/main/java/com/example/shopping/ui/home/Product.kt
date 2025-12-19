package com.example.shopping.ui.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,

    val imageKey :String,
    val imageResId: List<Int>,

    val price: Int,
    val description: String,
    var isFavorite: Boolean=false
) : Parcelable


