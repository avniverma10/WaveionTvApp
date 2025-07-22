package com.caastv.tvapp.extensions

import androidx.navigation.NavHostController

fun NavHostController.destinationExists(route: String): Boolean {
    return try {
        getBackStackEntry(route)
        true
    } catch (e: Exception) {
        false
    }
}