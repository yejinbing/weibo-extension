package com.googlecode.WeiboExtension;

import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.User;
import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader.ImageCallBack;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter{
	
	private static final String TAG = "UserListAdapter";
	
	public static final String USERLIST_USER_ID = "user_id";
	public static final String USERLIST_AVATAR_URL = "avatar_url";
	public static final String USERLIST_NAME = "name";
	public static final String USERLIST_DESCRIPTION = "description";
	public static final String USERLIST_INTRODUCE = "introduce";
	public static final String USERLIST_LATE_WEIBO = "late_weibo";
	public static final String USERlIST_IS_FOLLOWING = "is_following";
	
	public static final int LOGO_RES = R.drawable.weibo_listview_avatar;
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mUserList;
	
	public UserListAdapter(Context context, List<Map<String, Object>> userList) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mUserList = userList;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mUserList.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View converView, ViewGroup parent) {
		// TODO Auto-generated method stub
		UserList userList = null;
		if (converView == null) {
			userList = new UserList();
			converView = mInflater.inflate(R.layout.user_listview_item, null);
			userList.ivAvatar = (ImageView)converView.findViewById(R.id.user_listview_item_ivAvatar);
			userList.tvName = (TextView)converView.findViewById(R.id.user_listview_item_tvName);
			userList.tvDescription = (TextView)converView.findViewById(R.id.user_listview_item_tvDescription);
			userList.btnAttention = (Button)converView.findViewById(R.id.user_listview_item_btnAttention);
			converView.setTag(userList);
		}else {
			userList = (UserList) converView.getTag();
		}
		String avatarUrl = (String) mUserList.get(position).get(USERLIST_AVATAR_URL);
		Bitmap logoBitmap = new AsyncBitmapLoader(mContext).loadBitmap(userList.ivAvatar, avatarUrl, 
				new ImageCallBack() {
					
					public void imageLoad(ImageView imageView, Bitmap bitmap) {
						// TODO Auto-generated method stub
						imageView.setImageBitmap(bitmap);
					}
				});
		if (logoBitmap == null) {
			userList.ivAvatar.setImageResource(LOGO_RES);
		}else {
			userList.ivAvatar.setImageBitmap(logoBitmap);	
		}
		userList.tvName.setText((String) mUserList.get(position).get(USERLIST_NAME));
		userList.tvDescription.setText((String) mUserList.get(position).get(USERLIST_INTRODUCE));
		
		boolean isAttention = (Boolean) mUserList.get(position).get(USERlIST_IS_FOLLOWING);
		if (isAttention) {
			userList.btnAttention.setText(R.string.cancel_attention);
		}else {
			userList.btnAttention.setText(R.string.attention);
		}
		userList.btnAttention.setOnClickListener(
				new OnAttentionOperateListener(mContext, 
						(Long) mUserList.get(position).get(USERLIST_USER_ID), isAttention));
			
		return converView;
	} 
	
	public final class UserList {
		ImageView ivAvatar;
		TextView tvName;
		TextView tvDescription;
		Button btnAttention;
	}

	/**
	 * 添加关注或取消关注的按键监听器
	 * @author yejb
	 *
	 */
	private class OnAttentionOperateListener implements OnClickListener {

		private Context context;
		private long userId;
		private boolean isAttention;
		Weibo weibo = Weibo.getInstance();
		Button btnOperate;
		
		private static final int HANDLER_ATTENTION_MSG = 1;
		private static final String ATTENTION_CODE = "attention_code";
		private static final String ATTENTION_TOAST_MSG = "toast_msg";
		private static final int ATTENTION_CODE_SUCCESS = 0;	//操作成功
		private static final int ATTENTION_CODE_FAILURED = 1;	//操作失败
		
		public OnAttentionOperateListener(Context context, long userId, boolean isAttention) {
			this.context = context;
			this.userId = userId;
			this.isAttention = isAttention;
		}
		public void onClick(View view) {
			// TODO Auto-generated method stub
			
			btnOperate = (Button) view;
			Settings mSettings = new Settings(context);
			String token = mSettings.getToken();
			String tokenSecret = mSettings.getTokenSecret();	
			AccessToken accessToken = new AccessToken(token, tokenSecret);			
			weibo = Weibo.getInstance();
			weibo.setAccessToken(accessToken);
			
			new AttentionThread().start();
		}
		/**
		 * 对关注线程发送的结果进行分析
		 */
		Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case HANDLER_ATTENTION_MSG:
						Bundle bundle = msg.getData();
						int code = bundle.getInt(ATTENTION_CODE);
						if (code == ATTENTION_CODE_SUCCESS) {
						//取消关注成功
							if (isAttention) {
								isAttention = false;
								btnOperate.setText(R.string.attention);
								Utility.displayToast(context, R.string.cancel_attention_success);
						//添加关注成功
							}else {
								isAttention = true;
								btnOperate.setText(R.string.cancel_attention);
								Utility.displayToast(context, R.string.attention_success);
							}
						}else if (code == ATTENTION_CODE_FAILURED){
						//取消关注失败
							if (isAttention) {
								Utility.displayToast(context, R.string.cancel_attention_failure);
						//添加关注失败
							}else {
								Utility.displayToast(context, R.string.attention_failure);
							}
					//操作中出现错误，toast错误消息
						}else {
							Utility.displayToast(context, bundle.getString(ATTENTION_TOAST_MSG));
						}
						break;
		
					default:
						break;
				}
			}
		};
		/**
		 * 添加关注或取消关注的操作线程，通过handler通知操作结果
		 * @author yejb
		 *
		 */
		private class AttentionThread extends Thread {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				User user = null;
				Message msg = mHandler.obtainMessage();
				Bundle bundle = msg.getData();
				msg.what = HANDLER_ATTENTION_MSG;

				try {
					if (isAttention) {
						user = weibo.destroyFriendship(context, userId);
					}else {
						user = weibo.createFriendship(context, userId);
					}
				}catch (WeiboException e) {
					//TODO: handle exception
					e.printStackTrace();
					bundle.putInt(ATTENTION_CODE, e.getStatusCode());
					bundle.putString(ATTENTION_TOAST_MSG, e.getMessage());
					mHandler.sendMessage(msg);
					Thread.interrupted();
				}
				if (user != null) {
					bundle.putInt(ATTENTION_CODE, ATTENTION_CODE_SUCCESS);
					mHandler.sendMessage(msg);
				}else {
					bundle.putInt(ATTENTION_CODE, ATTENTION_CODE_FAILURED);
					mHandler.sendMessage(msg);
				}
			}
			
		}
	}
}
