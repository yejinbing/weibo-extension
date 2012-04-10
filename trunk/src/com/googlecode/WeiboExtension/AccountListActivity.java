package com.googlecode.WeiboExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.DialogError;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboDialogListener;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Account;
import sinaweibo4android.api.User;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.AccountDBAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class AccountListActivity extends Activity{

	private static final String TAG = "WeiboOauthSignPostActivity";
	
	private Context currentContext = AccountListActivity.this;
	    
	private Button btnAddUser;
	private ListView mUserListView;
	private List<Map<String, Object>> mUserList;
	private AccountListAdapter mUserListAdatper;
	
	Weibo weibo = Weibo.getInstance();
	 
	private int mPosition;
	
	private long defaultAccount;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userlist);
        
        initData();
        
        btnAddUser = (Button)findViewById(R.id.btn_add_user);
        mUserListView = (ListView)findViewById(R.id.user_list);
        
        boolean hasUser = getUserList();
     
        btnAddUser.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
		    	addUser();				
			}
		});
        if (!hasUser) {
        	btnAddUser.setVisibility(View.VISIBLE);
        } else {
        	btnAddUser.setVisibility(View.GONE);
        }
      
    }   
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.userlist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.menu_add_user:
				addUser();
				break;
			case R.id.menu_home:
				if (defaultAccount != 0) {
					Intent intent = new Intent(currentContext, HomeActivity.class);
					startActivity(intent);
					finish();
				}else {
					new AlertDialog.Builder(currentContext)
						.setMessage(R.string.select_default_account_remind)
						.setPositiveButton(R.string.ok, null)
						.show();
				}
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
   
    private void initData() {
    	//判断是否安装后第一次启动
    	SharedPreferences settings = getSharedPreferences("config", Context.MODE_PRIVATE);
    	defaultAccount = settings.getLong("default_account", 0);
    	
    }
    
    private void addUser() {
    	 // !!Don't forget to set app_key and secret before get
         // token!!!
         weibo.setupConsumerConfig(Constants.SinaConstants.CONSUMER_KEY, 
        		 Constants.SinaConstants.CONSUMER_SECRET);

         // Oauth2.0 隐式授权认证方式
         weibo.setRedirectUrl("http://www.sina.com");
         weibo.authorize(AccountListActivity.this, new AuthDialogListener());
    }
    
    class AuthDialogListener implements WeiboDialogListener {

		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, Constants.SinaConstants.CONSUMER_KEY);
			accessToken.setExpiresIn(expires_in);
			weibo.setAccessToken(accessToken);
			
			long userId = 0;
			try {
				Account account = weibo.getAccount(currentContext);
				userId = account.getId();
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
			}
			
			if (userId != 0) {
				User user = null;
				AccessInfo accessInfo;
				try {
					user = weibo.showUser(currentContext, userId);
					accessInfo = AccessInfo.getInstance();
					accessInfo.setUserId(user.getId());
					accessInfo.setAccessToken(token);
					accessInfo.setConsumerKey(Constants.SinaConstants.CONSUMER_SECRET);
					accessInfo.setScreenName(user.getScreenName());
					accessInfo.setWeiboMark(AccountDBAdapter.WEIBO_MARK_SINA);
					
					AccountDBAdapter dbAdapter = AccountDBAdapter.getInstance(AccountListActivity.this);
					dbAdapter.saveUserToken(accessInfo);					
					dbAdapter.saveUserInfo(user);
					Cursor cursor = null;
					try {
						cursor = dbAdapter.queryUserToken();
					//只有新增的这一个账户，自动设为默认账户,然后转向微博界面
						if (cursor.getCount() == 1) {
							SharedPreferences settings = getSharedPreferences("config", Context.MODE_PRIVATE);
				    		SharedPreferences.Editor editor = settings.edit();
				    		editor.putLong("default_account", userId);
				    		editor.commit();
				    		
				    		Intent toIntent = new Intent(currentContext, HomeActivity.class);
							toIntent.putExtra("user_id", userId);
				            startActivity(toIntent);
				            finish();
						}else {
						//不止这一个账户，则返回账户列表，并更新列表
							Map<String, Object> map = new HashMap<String, Object>();
							map.put(AccountListAdapter.USERLIST_LOGO, accessInfo.getWeiboMark());
			    			map.put(AccountListAdapter.USERLIST_ID, userId);
			    			map.put(AccountListAdapter.USERLIST_NAME, accessInfo.getScreenName());
			    			map.put(AccountListAdapter.USERLIST_DEFAULT, false);
			    			mUserList.add(map);
			    			mUserListAdatper.notifyDataSetChanged();
						}
					}catch (Exception e) {
						// TODO: handle exception
					}finally {
						if (cursor != null) {
							cursor.close();
						}
					}
				} catch (WeiboException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		public void onWeiboException(WeiboException e) {
			// TODO Auto-generated method stub
			Utility.displayToast(currentContext, "Auth exception : " + e.getMessage());
		}

		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Utility.displayToast(currentContext,  "Auth error : " + e.getMessage());		
		}

		public void onCancel() {
			// TODO Auto-generated method stub
			Utility.displayToast(currentContext, "Auth cancel");
		}

    }
    
    private boolean getUserList() {
    	
    	mUserList = new ArrayList<Map<String,Object>>();
    	
    	AccountDBAdapter dbAdapter = AccountDBAdapter.getInstance(AccountListActivity.this);
    	Cursor cursor = null;
    	try {
    		cursor = dbAdapter.queryUserToken();
    		while (cursor.moveToNext()) {
    			Map<String, Object> map = new HashMap<String, Object>();
    			long userId = cursor.getLong(cursor.getColumnIndex(AccountDBAdapter.KEY_USER_ID));
    			map.put(AccountListAdapter.USERLIST_LOGO, cursor.getInt(cursor.getColumnIndex(AccountDBAdapter.KEY_WEIBO_MARK)));
    			map.put(AccountListAdapter.USERLIST_ID, userId);
    			map.put(AccountListAdapter.USERLIST_NAME, cursor.getString(cursor.getColumnIndex(AccountDBAdapter.KEY_SCREEN_NAME)));
    			if (defaultAccount == userId) {
    				map.put(AccountListAdapter.USERLIST_DEFAULT, true);
    			}else {
    				map.put(AccountListAdapter.USERLIST_DEFAULT, false);
    			}
    			mUserList.add(map);
    		}
    	}catch (Exception e) {
			// TODO: handle exception
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
    	
    	if (mUserList != null && mUserList.size() != 0) {
    		mUserListAdatper = new AccountListAdapter(AccountListActivity.this, mUserList);
        	mUserListView.setAdapter(mUserListAdatper);
/*        	mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(UserListActivity.this, TimeLineActivity.class);
					intent.putExtra("user_id", (Long) mUserList.get(position).get(UserListAdapter.USERLIST_ID));
					startActivity(intent);
					finish();
				}
			});*/
        	mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					mPosition = position;
					String[] options;
					if ((Boolean) mUserList.get(mPosition).get(AccountListAdapter.USERLIST_DEFAULT)) {
						options = getResources().getStringArray(R.array.userlist_options_cancel);
					}else {
						options = getResources().getStringArray(R.array.userlist_options);
					}
					View optionsDialog = getLayoutInflater().inflate(R.layout.options_dialog, null);
					
					new AlertDialog.Builder(currentContext)
						.setView(optionsDialog)
						.setItems(options, new optionsOnClick())
						.show();
					return true;
				}
			});
        	
        	return true;
        }
    	
    	return false;
    }
    
    private class optionsOnClick implements DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			//delete
				case 0:
					deleteUser();				
					break;
			//set as default
				case 1:
					if ((Boolean) mUserList.get(mPosition).get(AccountListAdapter.USERLIST_DEFAULT)) {
				//将该账户取消设为默认	
						defaultAccount = 0;
						SharedPreferences settings = getSharedPreferences("config", Context.MODE_PRIVATE);
			    		SharedPreferences.Editor editor = settings.edit();
			    		editor.putLong("default_account", defaultAccount);
			    		editor.commit();
			    		Map<String, Object> map = mUserList.get(mPosition);
			    		map.put(AccountListAdapter.USERLIST_DEFAULT, false);
					}else { 
				//将该账户设为默认
						long userId = (Long) mUserList.get(mPosition).get(AccountListAdapter.USERLIST_ID);
						defaultAccount = userId;
						SharedPreferences settings = getSharedPreferences("config", Context.MODE_PRIVATE);
			    		SharedPreferences.Editor editor = settings.edit();
			    		editor.putLong("default_account", defaultAccount);
			    		editor.commit();
			    		Map<String, Object> map = mUserList.get(mPosition);
			    		map.put(AccountListAdapter.USERLIST_DEFAULT, true);
					}
					mUserListAdatper.notifyDataSetChanged();
					break;
			//modify
				case 2:
					break;
				default:
					break;
			}
		}
    	
    }
    
    private void deleteUser() {
    	new AlertDialog.Builder(currentContext)
    		.setTitle(R.string.delete_user)
    		.setMessage(R.string.delete_user_remind)
    		.setPositiveButton(R.string.ok, new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					//删除数据库中该用户信息
					long userId = (Long) mUserList.get(mPosition).get(AccountListAdapter.USERLIST_ID);
					AccountDBAdapter dbAdapter = AccountDBAdapter.getInstance(currentContext);
					dbAdapter.deleteUserToken(userId);
					dbAdapter.deleteUserInfo(userId);
					//删除该用户的微博信息缓存
					File file = new File(currentContext.getCacheDir().getPath() + "/mTimeLine" + userId);
					if (file.exists()) {
						file.delete();
					}
					//删除用户列表中该用户显示项
					mUserList.remove(mPosition);
					if (mUserList.size() == 0) {
						btnAddUser.setVisibility(View.VISIBLE);
					}
					mUserListAdatper.notifyDataSetChanged();
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.show();
    	
    }

}