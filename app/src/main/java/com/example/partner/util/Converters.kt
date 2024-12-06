package com.example.partner.util

import android.content.res.Resources

class Converters {
    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}