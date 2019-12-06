package com.helixtech.nfctest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Xml;
import android.widget.Button;
import android.widget.TextView;

public class readActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    //private static String tagNum=null;
    TextView dataTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        dataTxt = findViewById(R.id.data_txt);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0,intent,0);

    }

    @Override
    protected void onPause() {
        if(nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(nfcAdapter!=null){
            nfcAdapter.enableForegroundDispatch(this, pendingIntent,null,null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag!=null){
            byte[] byteData = tag.getId();

            dataTxt.setText(byteArrayToString(byteData));
            //tagNum = byteArrayToString(byteData);
        }

    }

    //Convert to hexadecimal
    public static final String CHARS = "0123456789ABCDEF";
    public static String byteArrayToString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<b.length;i++){
            sb.append(CHARS.charAt((b[i] >> 4) & 0x0F)).append(CHARS.charAt(b[i] & 0x0F));
        }
        return sb.toString();
    }






}