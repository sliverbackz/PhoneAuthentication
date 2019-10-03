package com.zmt.jacknephilim.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
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
import kotlinx.android.synthetic.main.activity_n.*
import kotlinx.android.synthetic.main.view_input_otp.*
import kotlinx.android.synthetic.main.view_input_phone_number.*
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NephilimActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String = ""
    private var authCredential: PhoneAuthCredential? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpCallback()
        setContentView(R.layout.activity_n)
        bindFromIntent()
        setListeners()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun setUpCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                layout_loading.gone()
                authCredential = credential
                Timber.i("SMS code %s", credential.smsCode)
                otp_view?.setText(credential.smsCode)
                if (credential.smsCode == null) {
                    signInWithPhoneAuthCredential(authCredential!!)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                layout_loading.gone()
                if (e is FirebaseAuthInvalidCredentialsException) {
                    et_phone_no?.error = "Invalid phone number"
                } else if (e is FirebaseTooManyRequestsException) {
                    et_phone_no?.error =
                        "Your request is unusual request. So blocked your phone number."
                }
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                resendToken = token
                btn_use_sms?.disabled()
                startTimer()
                layout_phone.gone()
                layout_loading.gone()
                layout_otp.show()
                Timber.i("onCodeSent token Id %s %s", token.toString(), vId)
                tv_code_send_description.text = getString(
                    R.string.lbl_verify_code_send_description,
                    String.format("+%s%s", ccp.selectedCountryCode, et_phone_no.text.toString())
                )
            }
        }
    }

    private fun bindFromIntent() {
        intent?.apply {
            et_phone_no.setText(extraString(extraPhoneNumber))
            if (et_phone_no.text.toString().isNotEmpty()) {
                if (authCredential == null && verificationId.isEmpty())
                    verifyPhone()
            }

//            tv_terms_of_service.apply {
//                text = toFormattedHtmlLink(R.string.lbl_terms_of_service)
//                guessVisibility(extraString(extraTermsOfService).isNotEmpty())
//                //visibility = if (it.extraString(extraTermsOfService).isEmpty()) View.GONE else View.VISIBLE
//            }

        }
    }

    private fun setListeners() {
        et_phone_no.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                btn_use_sms.isEnabled = et_phone_no.text.toString().length > 5
            }
        )

        otp_view.setOtpCompletionListener(
            object : OnOtpCompletionListener {
                override fun onOtpCompleted(otp: String?) {
                    btn_continue.enable()
                    tv_otp_code_error.gone()
                }

                override fun onOtpUnCompleted() {
                    btn_continue.disabled()
                }
            }
        )

        btn_use_sms.setOnClickListener {
            availableConnection(btn_use_sms) {
                if (authCredential == null && verificationId.isEmpty()) {
                    verifyPhone()
                }
            }
        }

        btn_continue.setOnClickListener {
            if (verificationId.isNotEmpty())
                authCredential = PhoneAuthProvider.getCredential(
                    verificationId,
                    otp_view.text.toString()
                )
            signInWithPhoneAuthCredential(authCredential!!)
        }

        btn_resend_code.setOnClickListener {
            availableConnection(btn_resend_code) {
                resendCode()
            }
        }
    }

    private fun resendCode() {
        layout_loading.show()
        val phoneNo = et_phone_no.text.toString()
        if (!phoneNo.isBlank()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                String.format("+%s%s", ccp.selectedCountryCode, phoneNo),
                60,
                TimeUnit.SECONDS,
                this,
                callbacks,
                resendToken
            )
            btn_resend_code.disabled()
        } else {
            et_phone_no.snackbar("Enter your phone number.")
        }

    }

    private fun verifyPhone() {
        layout_loading.show()
        val phoneNo = et_phone_no.text.toString()
        if (!phoneNo.isBlank()) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                String.format("+%s%s", ccp.selectedCountryCode, phoneNo),
                60,
                TimeUnit.SECONDS,
                this,
                callbacks
            )
        } else {
            et_phone_no.snackbar("Enter your phone number.")
        }

    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        layout_loading.show()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                layout_loading.gone()
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
        btn_resend_code.isEnabled = false
        object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                btn_resend_code.apply {
                    isEnabled = true
                    text = "Resend code"
                }
            }

            override fun onTick(p0: Long) {
                btn_resend_code.text =
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
