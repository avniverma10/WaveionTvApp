package com.example.tvapp.models


import androidx.room.Embedded
import androidx.room.Relation

data class ChannelWithPrograms(
    @Embedded val channel: EPGChannel,
    @Relation(
        parentColumn = "id",
        entityColumn = "channelId"
    )


    val programs: List<EPGProgram>
)
