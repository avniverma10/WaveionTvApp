package com.android.panmetroiptv.view.uicomponent.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun HideKeyboardOnEnter() {
  val view       = LocalView.current
  val focusMgr   = LocalFocusManager.current
  val kbCtrl     = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    // 1️⃣ remove focus so no TextField is “active”
    focusMgr.clearFocus(force = true)
    // 2️⃣ ask Compose to hide IME
    kbCtrl?.hide()
    // 3️⃣ fallback via WindowInsets
    ViewCompat.getWindowInsetsController(view)
      ?.hide(WindowInsetsCompat.Type.ime())
  }
}