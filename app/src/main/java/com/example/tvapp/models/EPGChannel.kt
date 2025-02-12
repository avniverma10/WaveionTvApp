package com.example.tvapp.models

data class EPGChannel(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val programs: List<EPGProgram>
)
