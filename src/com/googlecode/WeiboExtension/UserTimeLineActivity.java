package com.googlecode.WeiboExtension;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class UserTimeLineActivity extends Activity{

	private static final String		TAG					= "UserTimeLineActivity";
	
	public static final String		EXTRA_USER_ID		= "weibo_id";
	public static final String		EXTRA_SCREEN_NAME	= "screen_name";
	
	private ImageView				titlebarBack;
	private TextView				titlebarName;
	private ImageView				titlebarHome;
	
	private UserTimeLineListView	mUserTimeLineListView;
	
	private long					userId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_timeline);
		
		titlebarBack = (ImageView)findViewById(R.id.titlebar_ivLeft);
        titlebarName = (TextView)findViewById(R.id.titlebar_tvMiddle);
        titlebarHome = (ImageView)findViewById(R.id.titlebar_ivRight);
        titlebarBack.setImageResource(R.drawable.titlebar_back);
        titlebarHome.setImageResource(R.drawable.titlebar_home);
        titlebarBack.setOnClickListener(new OnUserTimeLineTitleBarListener());
        titlebarHome.setOnClickListener(new OnUserTimeLineTitleBarListener());
		
		mUserTimeLineListView =  (UserTimeLineListView)findViewById(R.id.user_timeline_list);
		
		if (savedInstanceState != null) {
			
		}else {
			Intent intent = getIntent();
			userId = intent.getLongExtra(EXTRA_USER_ID, 0);
			String screenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
			if (userId != 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "intent,user id:" + userId);
				}
				mUserTimeLineListView.setUserId(userId);
			}else {
				Log.e(TAG, "intent: invalid user id");
			}
			titlebarName.setText(screenName + " " + getResources().getString(R.string.weibo));
		}
		
		mUserTimeLineListView.onRefresh();
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		mUserTimeLineListView.resume();
	}

	private class OnUserTimeLineTitleBarListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
				case R.id.titlebar_ivLeft:
					finish();
					break;
				case R.id.titlebar_ivRight:

					break;
				default:
					break;
			}
		}
		
	}
}
