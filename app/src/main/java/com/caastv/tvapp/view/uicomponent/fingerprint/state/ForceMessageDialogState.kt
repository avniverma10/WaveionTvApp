package com.caastv.tvapp.view.uicomponent.fingerprint.state

import com.caastv.tvapp.model.data.sseresponse.ForceMessage

//First, create a data class to manage dialog state
data class ForceMessageDialogState(
    val message: ForceMessage,
    var show: Boolean = false
)