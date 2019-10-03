package com.zmt.jacknephilim.utils


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.text.Spanned
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zmt.jacknephilim.R
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

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

fun Activity.makeFullScreen() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
}


fun Context.toFormattedHtmlLink(@StringRes stringRes: Int): Spanned {
    return HtmlCompat.fromHtml(
        String.format("<u>%s</u>", getString(stringRes)),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}

fun Context.availableConnection(view: View? = null, yes: () -> Unit) {
    if (!this.isInternetAvailable()) {
        if (view == null) {
            this.toast("No internet connection.")
        } else {
            if (view is SwipeRefreshLayout) {
                view.isRefreshing = false
            }
            view.snackbar("No internet connection.")
        }
    } else {
        yes()
    }
}


object Utils {
    fun goneViews(vararg views: View) {
        views.forEach { it.gone() }
    }

}

