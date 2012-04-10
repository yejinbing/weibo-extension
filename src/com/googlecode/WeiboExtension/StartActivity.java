package com.googlecode.WeiboExtension;

import com.googlecode.WeiboExtension.EventStream.SNSSamplePluginConfig;
import com.googlecode.WeiboExtension.EventStream.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity{
	
	private static final String TAG = "StartActivity";
	
	private Context currentContext = StartActivity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
    	Intent intent;
    	
    	Settings settings = new Settings(currentContext);
    	long userId = settings.getOwnId();
    	if (userId != 0) {
    		intent = new Intent(this, HomeActivity.class);
    	}else {
    		intent = new Intent(this, SNSSamplePluginConfig.class);
    	}
    	startActivity(intent);
    	
    	finish();
	}
}
