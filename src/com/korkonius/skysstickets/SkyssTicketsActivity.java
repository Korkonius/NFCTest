package com.korkonius.skysstickets;

import java.io.IOException;
import java.util.Formatter;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Intent;

public class SkyssTicketsActivity extends Activity {
	
	static final String TAG = "ViewTag";
	//static final int ACTIVITY_TIMEOUT_MS = 1*1000;
	
	TextView statusView;
	NfcAdapter adapter;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        statusView = (TextView) findViewById(R.id.statusUpdates);
        adapter = NfcAdapter.getDefaultAdapter();
        resolveIntent(getIntent());
    }
    
    void resolveIntent(Intent intent) {
    	
    	// Parse intent
    	String action = intent.getAction();
    	if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
    		
    		// Parse Mifare Ultralight card
    		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    		MifareUltralight card = MifareUltralight.get(tag);
    		String s = "";
    		try {
    			card.connect();
    			byte[] data = card.readPages(0);
    			s = "Tag header " + bytesToHexString(data);
    			card.close();
    		}
    		catch(IOException e) {
    			s = "Failed to fetch data!";
    		}
    		statusView.setText("Tag discovered! " + s);
    	}
    	else {
    		Log.e(TAG, "Unknown intent " + intent);
            finish();
            return;
    	}
    }
    
    public static String bytesToHexString(byte[] bytes) {  
        StringBuilder sb = new StringBuilder(bytes.length * 2);  
      
        Formatter formatter = new Formatter(sb);  
        for (byte b : bytes) {  
            formatter.format("%02x", b);  
        }  
      
        return sb.toString();  
    }  
}