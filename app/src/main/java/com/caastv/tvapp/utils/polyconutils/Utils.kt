/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.caastv.tvapp.utils.polyconutils

import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast

/**
 * A collection of utility methods, all static.
 */
object Utils {
    /**
     * Returns the screen/display size
     */
    fun getDisplaySize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    fun convertDpToPixel(ctx: Context, dp: Int): Int {
        val density = ctx.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }



    fun showSimpleToast(context: Context?, msg: String?) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.show()

        val handler = Handler()
        handler.postDelayed({ toast.cancel() }, 500)
    }
}