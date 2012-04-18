package com.googlecode.WeiboExtension;

import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.User;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader.ImageCallBack;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class UserProfileActivity extends Activity{
	
	private static final String		TAG					= "UserProfileActivity";
	
	private Context					currentContext		= UserProfileActivity.this;
	
	public static final String		EXTRA_USER_ID		= "weibo_id";
	public static final String		EXTRA_SCREEN_NAME	= "screen_name";
	public static final String		EXTRA_AVATAR_URL	= "aravar_url";
	
	public static final int			LOGO_RES			= R.drawable.weibo_listview_avatar;
	public static final int			IMAGE_RES			= R.drawable.weibo_listview_pic_loading;
	
	private long					userId;
	private boolean					isAttention;
	
	private ImageView				userAvatar;
	private TextView				userName;
	private ImageView				userGender;
	private Button					btnOperate;
	private TextView				userAddress;
	private TextView				userLoginName;
	private TextView				userIntroduce;
	private TextView				tvAttentionCount;
	private TextView				tvWeiboCount;
	private TextView				tvFansCount;
	private TextView				tvTopicCount;
	
	private LinearLayout			llAttention;
	private LinearLayout			llWeibo;
	private LinearLayout			llFans;
	private LinearLayout			llTopic;
	
	private Weibo					mWeibo;
	
	
	private LoadUserProfileTask loadUserProfileTask = new LoadUserProfileTask();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);
		
		userAvatar = (ImageView)findViewById(R.id.user_profile_avatar);
		userName = (TextView)findViewById(R.id.user_profile_screen_name);
		userGender = (ImageView)findViewById(R.id.user_profile_gender);
		btnOperate = (Button)findViewById(R.id.btnOperate);
		userAddress= (TextView)findViewById(R.id.user_profile_address);
		userLoginName = (TextView)findViewById(R.id.user_profile_loginname);
		userIntroduce = (TextView)findViewById(R.id.user_profile_introduce);
		tvAttentionCount = (TextView)findViewById(R.id.tvAttention_count);
		tvWeiboCount = (TextView)findViewById(R.id.tvWeibo_count);
		tvFansCount = (TextView)findViewById(R.id.tvFans_count);
		tvTopicCount = (TextView)findViewById(R.id.tvTopic_count);
		
		llAttention = (LinearLayout)findViewById(R.id.llAttention);
		llWeibo = (LinearLayout)findViewById(R.id.llWeibo);
		llFans = (LinearLayout)findViewById(R.id.llFans);
		llTopic = (LinearLayout)findViewById(R.id.llTopic);
		
		init();
		
		btnOperate.setOnClickListener(new OnAttentionOperateListener());
		llAttention.setOnClickListener(new OnAttentionClickListener());
		llFans.setOnClickListener(new OnFansClickListener());
		llWeibo.setOnClickListener(new OnWeiboClickListener());
		
		if (savedInstanceState != null) {
			
		}else {
			Intent intent = getIntent();
			userId = intent.getLongExtra(EXTRA_USER_ID, 0);
			String avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL);
			String screenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
			if (userId != 0) {
				
			}else {
				Log.e(TAG, "onCreate,intent:no user id");
			}
			if (avatarUrl != null) {
				Bitmap logoBitmap = new AsyncBitmapLoader(currentContext).loadBitmap(userAvatar, avatarUrl, 
						new ImageCallBack() {
							
							public void imageLoad(ImageView imageView, Bitmap bitmap) {
								// TODO Auto-generated method stub
								imageView.setImageBitmap(bitmap);
							}
						});
				if (logoBitmap == null) {
					userAvatar.setImageResource(LOGO_RES);
				}else {
					userAvatar.setImageBitmap(logoBitmap);	
				}
			}
			if (screenName != null) {
				userName.setText(screenName);
			}
			
			loadUserProfileTask.execute();
		}
	}
	
	private void init() {
		Settings mSettings = new Settings(currentContext);
		String token = mSettings.getToken();
		String tokenSecret = mSettings.getTokenSecret();	
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		mWeibo = Weibo.getInstance();
		mWeibo.setAccessToken(accessToken);
	}
	
	private class LoadUserProfileTask extends AsyncTask<Void, Void, User> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected User doInBackground(Void... params) {
			// TODO Auto-generated method stub
			User user = null;
			try {
				user = mWeibo.showUser(currentContext, userId);
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return user;
		}
		
		@Override
		protected void onPostExecute(User result) {
			// TODO Auto-generated method stub
			if (result != null) {
				refreshUserProfile(result);
			}else {
				
			}
			super.onPostExecute(result);
		}
		
	}
	
	private void refreshUserProfile(User user) {
		userName.setText(user.getScreenName());
		String gender = user.getGender();
		if (gender.equals("m")) {	//男
			userGender.setImageResource(R.drawable.icon_male);
		}else if (gender.equals("f")) {	//女
			userGender.setImageResource(R.drawable.icon_female);
		}else if (gender.equals("n")) {	//未知
			userGender.setImageResource(R.drawable.icon_male);
		}else {
			userGender.setImageResource(R.drawable.icon_male);
			Log.w(TAG, "refreshUserProfile: invalid gender");
		}
		if (user.isFollowing()) {
			isAttention = true;
			btnOperate.setText(R.string.cancel_attention);
		}else {
			isAttention = false;
			btnOperate.setText(R.string.attention);
		}
		userAddress.setText(user.getLocation());
		userIntroduce.setText(user.getDescription());
		userLoginName.setText(user.getId() + "");
		
		tvAttentionCount.setText(user.getFriendsCount() + "");
		tvWeiboCount.setText(user.getStatusesCount() + "");
		tvFansCount.setText(user.getFollowersCount() + "");
	}
	
	private class OnAttentionOperateListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			new AsyncTask<Void, Void, Object>() {

				@Override
				protected Object doInBackground(Void... arg0) {
					// TODO Auto-generated method stub
					User user = null;
					try {
						if (isAttention) {
							user = mWeibo.destroyFriendship(currentContext, userId);
						}else {
							user = mWeibo.createFriendship(currentContext, userId);
						}
					}catch (WeiboException e) {
						//TODO: handle exception
						e.printStackTrace();
						return e;
					}
					return user;
				}

				@Override
				protected void onPostExecute(Object result) {
					// TODO Auto-generated method stub
					if (result instanceof User) {
						if (result != null) {
					//取消关注成功
							if (isAttention) {
								isAttention = false;
								btnOperate.setText(R.string.attention);
								Utility.displayToast(currentContext, R.string.cancel_attention_success);
					//添加关注成功
							}else {
								isAttention = true;
								btnOperate.setText(R.string.cancel_attention);
								Utility.displayToast(currentContext, R.string.attention_success);
							}
						}
					}else if (result instanceof WeiboException) {
						WeiboException e = (WeiboException) result;
						Utility.displayToast(currentContext, e.getMessage());
					}
					
					super.onPostExecute(result);
				}
				
			}.execute();
		}
		
	}
	
	private class OnAttentionClickListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(currentContext, UserFriendsActivity.class);
			intent.putExtra(UserFriendsActivity.EXTRA_USER_ID, userId);
			intent.putExtra(UserFriendsActivity.EXTRA_SCREEN_NAME, userName.getText());
			currentContext.startActivity(intent);
		}
		
	}
	
	private class OnFansClickListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(currentContext, UserFollowersActivity.class);
			intent.putExtra(UserFollowersActivity.EXTRA_USER_ID, userId);
			intent.putExtra(UserFollowersActivity.EXTRA_SCREEN_NAME, userName.getText());
			currentContext.startActivity(intent);
		}
		
	}
	
	private class OnWeiboClickListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(currentContext, UserTimeLineActivity.class);
			intent.putExtra(UserTimeLineActivity.EXTRA_USER_ID, userId);
			intent.putExtra(UserTimeLineActivity.EXTRA_SCREEN_NAME, userName.getText());
			currentContext.startActivity(intent);
		}
		
	}

}
