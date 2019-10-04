package com.zmt.jacknephilim.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zmt.jacknephilim.R
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_custom_auth.setOnClickListener {
            startActivity<FirstActivity>()
        }

        btn_own_view.setOnClickListener {
            startActivity<NephilimActivity>()
        }
    }

}
