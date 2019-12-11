package com.helixtech.nfctest

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_read.*
import java.lang.StringBuilder

import java.nio.charset.Charset
import kotlin.experimental.and


class readActivity : AppCompatActivity() {

    internal var nfcAdapter: NfcAdapter? = null
    internal lateinit var pendingIntent: PendingIntent
    internal var result: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

    }

    override fun onPause() {
        nfcAdapter?.let {
            it.disableForegroundDispatch(this)
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter?.let {
            it.enableForegroundDispatch(this, pendingIntent,null,null)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val ndef = Ndef.get(tag)

        val size = ndef.maxSize
        val type = ndef.type

        //val id = byteArrayToString(tag!!.id)

        val byteData: ByteArray = tag.id

        var sb: StringBuilder = StringBuilder();
        for (i in byteData) {
            sb.append(String.format("%02X", i and 0xff.toByte()))
        }
        val id:String = sb.toString()

        data_txt.text = id




        try {
            val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            Toast.makeText(this, "Here : $messages", Toast.LENGTH_SHORT).show()
            val size = messages.size
            Log.d("SIZEE", size.toString())
            for (i in 0..1)
                readTagData(messages[0] as NdefMessage)
        } catch (e: Exception) {
            Log.d("EEEROR", e.printStackTrace().toString())
        }

        Toast.makeText(this, "Data : $result", Toast.LENGTH_SHORT).show()
        data_txt2.text = result

    }

    fun readTagData(ndefmsg: NdefMessage) {

        ndefmsg?.let {

            var msgs = ""
            msgs += ndefmsg.toString() + "\n"
            val records = ndefmsg.records

            for (rec in records) {
                val payload = rec.payload
                var textEncoding = "UTF-8"
                if (payload.size > 0)
                    textEncoding = if ((payload[0] and 128.toByte()).toInt()==0) "UTF-8" else "UTF-16"

                val tnf = rec.tnf
                val type = rec.type.toString()
                val payloadStr = String(rec.payload, Charset.forName(textEncoding))
                result = payloadStr
                Toast.makeText(this, "$tnf : $type : $payloadStr", Toast.LENGTH_SHORT).show()
                /*Toast.makeText(this, "Data : $result", Toast.LENGTH_SHORT).show()
                data_txt2.text = result*/
            }
        }
    }

    companion object {

    }


}
