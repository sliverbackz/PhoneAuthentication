package com.kyawhtut.firebasephoneauthlib.util

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.kyawhtut.firebasephoneauthlib.R
import com.kyawhtut.firebasephoneauthlib.ui.PhoneAuthentication

class PhoneAuth private constructor(
    private var activity: Activity? = null,
    private var fm: Fragment? = null,
    var termsOfService: String = "",
    var privacyPolicy: String = "",
    var appName: String = "",
    var appLogo: Int = R.drawable.default_header,
    var headerImage: Int = R.drawable.default_header,
    var phoneNumber: String = ""
) : PhoneVerify {

    private val providers = arrayListOf(
        AuthUI.IdpConfig.PhoneBuilder().build()
    )

    companion object {
        private const val PHONE_AUTH = 0x1497
        private const val PHONE_AUTH_DEFAULT = 0x1498

        fun logout(ctx: Context, success: () -> Unit = {}, fail: (Exception) -> Unit = {}) {
            if (FirebaseAuth.getInstance().currentUser != null)
                AuthUI.getInstance().signOut(ctx).addOnCompleteListener {
                    if (it.isSuccessful) success()
                    else fail(it.exception ?: Exception("Something went wrong!!"))
                }
        }

        fun isLogin() = FirebaseAuth.getInstance().currentUser != null
    }

    override fun startActivity(phone: String, loginTheme: LoginTheme) {
        this.phoneNumber = phone
        startActivity(loginTheme)
    }

    override fun startActivity(loginTheme: LoginTheme) {
        if (activity == null && fm == null) throw IllegalArgumentException("Please set activity or fragment. Activity or Fragment must not be null.")
        if (activity != null) {
            if (loginTheme is LoginTheme.MaterialTheme)
                activity?.startActivityForResult(
                    getIntent(),
                    PHONE_AUTH
                )
            else {
                activity?.startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(appLogo)
                        .setTosAndPrivacyPolicyUrls(
                            if (termsOfService.isEmpty()) "https://joebirch.co/terms.html" else termsOfService,
                            if (privacyPolicy.isEmpty()) "https://joebirch.co/privacy.html" else privacyPolicy
                        )
                        .build(),
                    PHONE_AUTH_DEFAULT
                )
            }
        } else {
            if (loginTheme is LoginTheme.MaterialTheme)
                fm?.startActivityForResult(
                    getIntent(),
                    PHONE_AUTH
                )
            else {
                fm?.startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(appLogo)
                        .setTosAndPrivacyPolicyUrls(
                            if (termsOfService.isEmpty()) "https://joebirch.co/terms.html" else termsOfService,
                            if (privacyPolicy.isEmpty()) "https://joebirch.co/privacy.html" else privacyPolicy
                        )
                        .build(),
                    PHONE_AUTH_DEFAULT
                )
            }
        }
    }

    private fun getIntent(): Intent {
        if (activity == null && fm == null) Throwable("Please set activity or fragment. Activity or Fragment must not be null.")
        val intent =
            Intent(if (activity == null) fm?.context else activity, PhoneAuthentication::class.java)
        intent.apply {
            putExtra(PhoneAuthentication.extraTermsOfService, termsOfService)
            putExtra(PhoneAuthentication.extraPrivacyPolicy, privacyPolicy)
            putExtra(PhoneAuthentication.extraAppName, appName)
            putExtra(PhoneAuthentication.extraAppLogo, appLogo)
            putExtra(PhoneAuthentication.extraHeaderImage, headerImage)
            putExtra(PhoneAuthentication.extraPhoneNumber, phoneNumber)
        }
        this.phoneNumber = ""
        return intent
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        func: PhoneVerifyCallback
    ) {
        if (requestCode == PHONE_AUTH || requestCode == PHONE_AUTH_DEFAULT) {
            if (resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                func.Success(Phone(user?.uid ?: "", user?.phoneNumber ?: ""))
            } else {
                func.Error("Login canceled!!")
            }
        }
    }

    override fun logout(success: () -> Unit, fail: (Exception) -> Unit) {
        if (activity == null) throw IllegalArgumentException("Please set activity. Activity must not be null.")
        if (isLogin())
            AuthUI.getInstance().signOut(activity!!).addOnCompleteListener {
                if (it.isSuccessful) success()
                else fail(it.exception ?: Exception("Something went wrong!!"))
            }
    }

    class Builder private constructor(var activity: Activity?, var fragment: Fragment?) {

        constructor(fm: Fragment?) : this(null, fm)
        constructor(activity: Activity?) : this(activity, null)

        private lateinit var phoneAuth: PhoneAuth

        var termsOfService: String = ""
        var privacyPolicy: String = ""
        var appName: String = ""
        var appLogo: Int = R.drawable.default_header
        var headerImage: Int = R.drawable.default_header
        var phoneNumber: String = ""

        fun build(): PhoneAuth {
            phoneAuth = PhoneAuth(
                activity,
                fragment,
                termsOfService,
                privacyPolicy,
                appName,
                appLogo,
                headerImage,
                phoneNumber
            )
            return phoneAuth
        }
    }
}