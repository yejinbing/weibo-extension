package com.googlecode.WeiboExtension;

import com.googlecode.WeiboExtension.EventStream.Database;
import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsTimeLineActivity extends Activity{	


	private static final String TAG = "FriendsTimeLineActivity";
	
	private Context currentContext = FriendsTimeLineActivity.this;
	
	public static final int DIALOG_EXIT = 0;
	
	private FriendsTimeLineListView mWeiboListView;
	
	private ImageView titlebarRefresh;
	private TextView titlebarName;
	private ImageView titlebarEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (Constants.Config.DEBUG) {
			Log.d(TAG, "onCreate");
		}
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.friends_timeline);
		mWeiboListView = (FriendsTimeLineListView)findViewById(R.id.friends_timeline_list);
		titlebarRefresh = (ImageView)findViewById(R.id.titlebar_ivLeft);
        titlebarName = (TextView)findViewById(R.id.titlebar_tvMiddle);
        titlebarEdit = (ImageView)findViewById(R.id.titlebar_ivRight);
        titlebarRefresh.setImageResource(R.drawable.titlebar_refresh);
        titlebarEdit.setImageResource(R.drawable.titlebar_post_new_normal);
        titlebarRefresh.setOnClickListener(new OnTimeLineTitleBarListener());
        titlebarName.setOnClickListener(new OnTimeLineTitleBarListener());
        titlebarEdit.setOnClickListener(new OnTimeLineTitleBarListener());
        
        titlebarName.setText(AccessInfo.getInstance().getScreenName());
        
        Settings settings = new Settings(currentContext);
        titlebarName.setText(settings.getDisplayName());
        
        mWeiboListView.onRefresh();
        
		if (savedInstanceState == null) {

		}else {
			
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (Constants.Config.DEBUG) {
			Log.d(TAG, "onResume");
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (Constants.Config.DEBUG) {
			Log.d(TAG, "onDestroy");
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (Constants.Config.DEBUG) {
			Log.d(TAG, "onPause");
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (Constants.Config.DEBUG) {
			Log.d(TAG, "onStop");
		}
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.timeline_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.menu_clear_list:
				mWeiboListView.clearList();
				TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(currentContext);
				dbAdapter.cleanup();
				Database database = new Database(currentContext);
				database.cleanup();
				break;
			case R.id.menu_user_list:
				startActivity(new Intent(currentContext, AccountListActivity.class));
				break;
			case R.id.menu_settings:
				Intent intent = new Intent();
				intent.setClass(currentContext, SettingsActivity.class);
				startActivityForResult(intent, 0);
				break;
			case R.id.menu_help:
				break;
			case R.id.menu_about:
				final View aboutDialog = LayoutInflater.from(currentContext).inflate(R.layout.about_dialog, null);
				new AlertDialog.Builder(currentContext)
						.setView(aboutDialog)
						.setIcon(R.drawable.icon)
						.setTitle(R.string.app_name)
						.setPositiveButton(R.string.ok, null)
						.show();
				break;
			case R.id.menu_exit:
				finish();
				System.exit(0);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				showDialog(DIALOG_EXIT);
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		switch (id) {
			case DIALOG_EXIT:
				return setupExitDialog();
			default:
				return super.onCreateDialog(id, args);
		}
		
	}
	
	 /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private Dialog setupExitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.exit_dialog_msg);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	finish();
				System.exit(0);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        return dialog.create();
    }

	private class OnTimeLineTitleBarListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
				case R.id.titlebar_ivLeft:
					if (Constants.Config.DEBUG) {
						Log.d(TAG, "onClick:refresh");
					}
					mWeiboListView.onRefresh();
					break;
				case R.id.titlebar_tvMiddle:
					if (Constants.Config.DEBUG) {
						Log.d(TAG, "onClick:name");
					}
					break;
				case R.id.titlebar_ivRight:
					if (Constants.Config.DEBUG) {
						Log.d(TAG, "onClick:edit");
					}
					Intent intent = new Intent(currentContext, ShareActivity.class);
					Settings settings = new Settings(currentContext);
					String token = settings.getToken();
					String tokenSecret = settings.getTokenSecret();
					intent.putExtra(ShareActivity.EXTRA_ACCESS_TOKEN, token);
					intent.putExtra(ShareActivity.EXTRA_COSUMER_KEY, tokenSecret);
					startActivity(intent);
					break;
				default:
					break;
			}
		}
		
	}
}
