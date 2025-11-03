package com.panmetro.iptv.model.audiotrack

import androidx.media3.common.TrackGroup

data class AudioTrackInfo(
    val groupIndex: TrackGroup,
    val trackIndex: Int,
    val languageTag: String?, // e.g., "en", "hi", "ta-IN", may be null or "und"
    val label: String?        // e.g., "Hindi", "English 5.1", etc.
)