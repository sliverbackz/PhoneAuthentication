package com.zmt.jacknephilim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.view_phone_verification.*
import org.jetbrains.anko.toast
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import android.app.PendingIntent
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class FirstActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_phone_verification)
        ccp.selectedCountryCode?.apply {
            et_phone_no.setText("+$this")
        }

        ccp.setOnCountryChangeListener {
            et_phone_no.setText("+${ccp.selectedCountryCode}")
            et_phone_no.setSelection(et_phone_no.text.length)
        }

        et_phone_no.addTextChangedListener {
            if (it.isNullOrBlank()) {
                toast("Please select country code.")
            }
        }
    }

}
