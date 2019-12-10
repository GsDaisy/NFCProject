package com.helixtech.nfctest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;


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

        /*if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
        try {
            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            Toast.makeText(this,"여기야 : "+messages.toString(), Toast.LENGTH_SHORT).show();
            if (messages == null) return;

            for (int i = 0; i < messages.length; i++)
                readTagData((NdefMessage) messages[0]);
        }catch(Exception e){
            Log.e("NFC error", e+"");
        }*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /*Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag!=null){
            byte[] byteData = tag.getId();

            dataTxt.setText(byteArrayToString(byteData));
            //tagNum = byteArrayToString(byteData);
            processTag(intent);
        }*/
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);

        int size = ndef.getMaxSize();
        String type = ndef.getType();
        /*try {
            String text = ndef.getNdefMessage().toString();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

        } catch (FormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        String id = byteArrayToString(tag.getId());

        //Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }

            Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            Toast.makeText(this,"여기야 : "+messages.toString(), Toast.LENGTH_SHORT).show();
            if (messages == null) return;

            for (int i = 0; i < messages.length; i++)
                readTagData((NdefMessage) messages[0]);



    }

    public void readTagData(NdefMessage ndefmsg) {

        if(ndefmsg == null ) return ;
        String result=null;
        String msgs = "";
        msgs += ndefmsg.toString() + "\n";

        NdefRecord[] records = ndefmsg.getRecords() ;

        for(NdefRecord rec : records) {
            byte [] payload = rec.getPayload() ;
            String textEncoding = "UTF-8" ;
            if(payload.length > 0)
                textEncoding = ( payload[0] & 0200 ) == 0 ? "UTF-8" : "UTF-16";

            Short tnf = rec.getTnf();
            String type = String.valueOf(rec.getType());
            String payloadStr = new String(rec.getPayload(), Charset.forName(textEncoding));

            Toast.makeText(this, tnf+" : " +type+" : " +payloadStr, Toast.LENGTH_SHORT).show();
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


    private void processTag(Intent passedIntent){
        Parcelable[] rawMsgs = passedIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMsgs==null){
            return;
        }
        NdefMessage[] msgs;
        if(rawMsgs!=null){
            msgs = new NdefMessage[rawMsgs.length];
            for(int i=0;i<rawMsgs.length;i++){
                msgs[i]=(NdefMessage)rawMsgs[i];

            }
            dataTxt.setText(msgs.toString());
        }
    }




}