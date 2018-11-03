package com.jankin.smartgeography;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;


public class PushJankinReceiver extends BroadcastReceiver{
	
	private static final String TAG = "PushJankinReceiver";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	 	
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive() action=" + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) 
        {
            case PushConsts.GET_MSG_DATA:
                // 获取透传（payload）数据
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null)
                {
                    String data = new String(payload);
                    Log.d(TAG, "Got Payload:" + data);
                    // TODO:接收处理透传（payload）数据
                }
                break;
            case PushConsts.GET_CLIENTID:
            	String ClientID = bundle.getString("clientid");
            	if (ClientID != null) {
            		Log.d(TAG, "Got ClientID: " + ClientID);
            		if (mListener != null) {
            			mListener.getGetuiClientID(ClientID);
					}
				}
            	break;
            default:
                break;
        }
    }
    
    
    
    private static GetuiClientIDListener mListener = null;

    /**
     * @param listener
     */
    public static  void setGetuiClientIDListener(GetuiClientIDListener listener) {
        mListener = listener;
    }

    public interface GetuiClientIDListener{
		
		public void getGetuiClientID(String ClientID);
		
	}
}