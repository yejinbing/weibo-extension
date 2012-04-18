package com.googlecode.WeiboExtension;

import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import com.googlecode.WeiboExtension.EventStream.SNSSampleService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * 网络状态改变监听器，当有网络连接上时想SNSSampleService发送一个intent，根据是否设置自动刷新来刷新
 * @author yejb
 *
 */
public class NetWorkStateReceiver extends BroadcastReceiver{
	
	private static final String		TAG					= "NetWorkStateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
        if (Config.DEBUG) {
            Log.d(TAG, "Received broadcast: " + action);
        }
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
        	if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
    			Log.d(TAG, "netWork has lost"); 
    		}else {
    			Log.d(TAG, "netWork has connect"); 
    			Intent serviceIntent = new Intent(SNSSampleService.INTENT_ACTION_NET_CONNECT);
    			serviceIntent.setClass(context, SNSSampleService.class);
    			context.startService(serviceIntent);
    		}
        }
		
	}

}
