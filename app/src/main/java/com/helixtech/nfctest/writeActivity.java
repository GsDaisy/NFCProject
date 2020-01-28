package com.helixtech.nfctest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NdefFormatable;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;


import kotlin.text.Charsets;

import static android.view.PointerIcon.TYPE_TEXT;

public class writeActivity extends AppCompatActivity {


    private static final String TAG = "WriteActivity";
    private boolean mResumed = false;
    private boolean mWriteMode = false;
    NfcAdapter mNfcAdapter;
    EditText mNote;
    Intent mIntent;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        findViewById(R.id.write_tag).setOnClickListener(mTagWriter);
        mNote = findViewById(R.id.note);
        mNote.addTextChangedListener(mTextWatcher);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            mNdefExchangeFilters = new IntentFilter[]{ndefDetected};
        }
        IntentFilter tagDetected = new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[]{tagDetected};
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNCHANGED,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }
        } else {
            finish();
        }
        return msgs;
    }

    private void setNotBody(String string) {
        Editable text = mNote.getText();
        text.clear();
        text.append(string);
    }

    private void enableNdefExchageMode() {
        mNfcAdapter.enableForegroundNdefPush(writeActivity.this, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
        }
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(getNoteAsNdef(), detectedTag);
        }
    }

    boolean writeTag(NdefMessage message, Tag detectedTag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(detectedTag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                toast("태그에 기록 : " + message.toString());
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(detectedTag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private View.OnClickListener mTagWriter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            disableNdefExchangeMode();
            enableTagWriteMode();
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
            if (mResumed) {
                mNfcAdapter.enableForegroundNdefPush(writeActivity.this,
                        getNoteAsNdef());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    };

    protected NdefMessage getNoteAsNdef() {
        byte[] textBytes = mNote.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[]{}, textBytes);
        return new NdefMessage(new NdefRecord[]{textRecord});
    }

    protected void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(
                NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[]{tagDetected};
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mWriteTagFilters, null);
    }

    private void disableNdefExchangeMode() {
        mNfcAdapter.disableForegroundNdefPush(this);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundNdefPush(writeActivity.this, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }
}



