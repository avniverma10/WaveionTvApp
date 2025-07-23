package com.android.panmetroiptv.view.uicomponent.fingerprint.state

import com.android.panmetroiptv.model.data.sseresponse.ForceMessage

//First, create a data class to manage dialog state
data class ForceMessageDialogState(
    val message: ForceMessage,
    var show: Boolean = false
)