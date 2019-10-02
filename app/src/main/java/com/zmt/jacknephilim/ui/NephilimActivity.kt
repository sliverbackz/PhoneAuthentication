package com.zmt.jacknephilim.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.zmt.jacknephilim.R
import com.zmt.jacknephilim.components.OnOtpCompletionListener
import com.zmt.jacknephilim.utils.*
import kotlinx.android.synthetic.main.activity_nephilim.*
import kotlinx.android.synthetic.main.otp_view.*
import kotlinx.android.synthetic.main.view_loading.*
import java.util.concurrent.TimeUnit

class NephilimActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String = ""
    private var authCredential: PhoneAuthCredential? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setUpCallback()
        setContentView(R.layout.activity_nephilim)
        getDataAndBind()
        setListeners()
    }

    private fun setUpCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                fm_loading.gone()
                authCredential = credential
                if (credential.smsCode == null) {
                    signInWithPhoneAuthCredential(authCredential!!)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                fm_loading.gone()
                if (e is FirebaseAuthInvalidCredentialsException) {
                    et_phone_no.error = "Invalid phone number"
                } else if (e is FirebaseTooManyRequestsException) {
                    et_phone_no.error =
                        "Your request is unusual request. So blocked your phone number."
                }
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                resendToken = token
                btn_verify.apply {
                    isEnabled = false
                    text = getString(R.string.lbl_btn_continue)
                }
                startTimer()
                fm_loading.gone()
                tv_terms_of_service.gone()
                tv_privacy_policy.gone()
                // tv_app_name.text = getString(R.string.lbl_verification_code)
                group_phone_input.gone()
                layout_otp.show()
                tv_code_send_description.text = getString(
                    R.string.lbl_verify_code_send_description,
                    String.format("+%s%s", ccp.selectedCountryCode, et_phone_no.text.toString())
                )
            }
        }
    }

    private fun getDataAndBind() {
        intent?.apply {
            et_phone_no.setText(extraString(extraPhoneNumber))
            if (et_phone_no.text.toString().isNotEmpty()) {
                if (authCredential == null && verificationId.isEmpty())
                    verifyPhone()
            }
            tv_terms_of_service.apply {
                text = toFormattedHtmlLink(R.string.lbl_terms_of_service)
                guessVisibility(extraString(extraTermsOfService).isNotEmpty())
                //visibility = if (it.extraString(extraTermsOfService).isEmpty()) View.GONE else View.VISIBLE
            }
            tv_privacy_policy.apply {
                text = toFormattedHtmlLink(R.string.lbl_privacy_policy)
                guessVisibility(extraString(extraPrivacyPolicy).isNotEmpty())
            }
        }
    }

    private fun setListeners() {
        tv_terms_of_service.setOnClickListener {
            openBrowser(intent?.getStringExtra(extraTermsOfService) ?: "")
        }

        tv_privacy_policy.setOnClickListener {
            openBrowser(intent?.getStringExtra(extraPrivacyPolicy) ?: "")
        }

        et_phone_no.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                btn_verify.isEnabled = et_phone_no.text.toString().length > 5
            }
        )

        otp_view.setOtpCompletionListener(
            object : OnOtpCompletionListener {
                override fun onOtpCompleted(otp: String?) {
                    btn_verify.enable()
                    tv_otp_code_error.gone()
                }

                override fun onOtpUnCompleted() {
                    btn_verify.disabled()
                }

            }
        )

        btn_verify.setOnClickListener {
            if (authCredential == null && verificationId.isEmpty())
                verifyPhone()
            else {
                if (verificationId.isNotEmpty())
                    authCredential =
                        PhoneAuthProvider.getCredential(verificationId, otp_view.text.toString())
                signInWithPhoneAuthCredential(authCredential!!)
            }
        }

        btn_resend.setOnClickListener {
            resendCode()
        }
    }

    private fun resendCode() {
        fm_loading.show()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            String.format("+%s%s", ccp.selectedCountryCode, et_phone_no.text.toString()),
            60,
            TimeUnit.SECONDS,
            this,
            callbacks,
            resendToken
        )
        btn_resend.disabled()
    }

    private fun verifyPhone() {
        fm_loading.show()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            String.format("+%s%s", ccp.selectedCountryCode, et_phone_no.text.toString()),
            60,
            TimeUnit.SECONDS,
            this,
            callbacks
        )
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        fm_loading.show()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                fm_loading.gone()
                if (task.isSuccessful) {
                    SuccessDialog.Builder(this).apply {
                        this.message = "Auth Success"
                        isCancelable = false
                        duration = 1000
                        callback = {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }.show(supportFragmentManager, SuccessDialog::class.java.name)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        tv_otp_code_error.apply {
                            text = "Invalid code"
                            show()
                        }
                    }
                }
            }
    }

    private fun startTimer() {
        btn_resend.isEnabled = false
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                btn_resend.apply {
                    isEnabled = true
                    text = "Resend code"
                }
            }

            override fun onTick(p0: Long) {
                btn_resend.text =
                    getString(
                        R.string.lbl_btn_resend,
                        "%02d".format((p0 / 1000) / 60),
                        "%02d".format((p0 / 1000) % 60)
                    )
            }
        }.start()
    }

    private fun openBrowser(link: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    companion object {
        const val extraTermsOfService = "extra.termsOfService"
        const val extraPrivacyPolicy = "extra.privacyPolicy"
        const val extraPhoneNumber = "extra.phoneNumber"
    }
}
