package com.zmt.jacknephilim

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kyawhtut.firebasephoneauthlib.util.LoginTheme
import com.kyawhtut.firebasephoneauthlib.util.Phone
import com.kyawhtut.firebasephoneauthlib.util.PhoneAuth
import com.kyawhtut.firebasephoneauthlib.util.PhoneVerifyCallback
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
    private val phoneAuth: PhoneAuth = PhoneAuth.Builder(this).apply {
        appName = "Jack Nephilim"
    }.build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_auth.setOnClickListener {
            phoneAuth.startActivity(LoginTheme.FirebaseTheme)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        phoneAuth.onActivityResult(requestCode, resultCode, data, object : PhoneVerifyCallback {
            override fun Success(result: Phone) {
                toast(result.phone)
            }

            override fun Error(error: String) {
                toast(error)
            }
        })
    }
}
