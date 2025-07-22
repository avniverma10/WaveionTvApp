package com.caastv.tvapp.extensions

import java.text.SimpleDateFormat
import java.util.Locale


fun Long.formatTime(): String {
    return this.let {
        SimpleDateFormat("hh:mm", Locale.getDefault()).format(it)
    } ?: "--"
}