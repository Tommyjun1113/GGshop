package com.example.shopping.utils

import android.content.Context

object FavoriteManager {

    private const val PREF_NAME = "favorite_pref"
    private const val KEY_FAVORITES = "favorites"

    fun getFavorites(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?:mutableSetOf()
    }
    fun setFavorites(context: Context, ids: Set<String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putStringSet(KEY_FAVORITES, ids.toMutableSet())
            .apply()
    }
    fun isFavorite(context: Context, productId: String): Boolean {
        return getFavorites(context).contains(productId)
    }

    fun toggleFavorite(context: Context, productId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val set = getFavorites(context)

        val newState = if (set.contains(productId)) {
            set.remove(productId)
            false
        } else {
            set.add(productId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES,set).apply()
        if(UserSession.isLogin){
            if(newState){
                FavoriteRepository.addFavorite(productId)
            }else{
                FavoriteRepository.removeFavorite(productId)
            }
        }
        return newState
    }


}