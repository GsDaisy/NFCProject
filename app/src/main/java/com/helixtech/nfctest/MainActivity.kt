package com.helixtech.nfctest

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        read_btn.setOnClickListener {
            val intent = Intent(this, readActivity::class.java)
            startActivity(intent)
        }
    }
}
