package com.zmt.jacknephilim.utils


import android.content.Context
import android.content.Intent
import android.text.Spanned
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat

fun Button.enable() {
    isEnabled = true
}

fun Button.disabled() {
    isEnabled = false
}

fun View.gone() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun Intent.extraString(extra: String): String {
    return getStringExtra(extra) ?: ""
}

fun View.guessVisibility(isVisible: Boolean) {
    visibility = if (isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun Context.toFormattedHtmlLink(@StringRes stringRes: Int): Spanned {
    return HtmlCompat.fromHtml(
        String.format("<u>%s</u>", getString(stringRes)),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}

