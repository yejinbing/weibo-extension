package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.User;
import sinaweibo4android.api.UserWapper;
import android.content.Context;
import android.util.AttributeSet;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.View.AbsUserList;

public class UserFriendsListView extends AbsUserList{
	
	private static final String		TAG					= "UserFriendsListView";
	
	private Context					mContext;
	private Weibo					mWeibo;
	private long					userId;
	private int						nextCursor = 0;

	public UserFriendsListView(Context context) {
		super(context);
		init(context);
	}

	public UserFriendsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public UserFriendsListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		super.onRefresh();
	}

	@Override
	protected List<Map<String, Object>> getHeader() throws WeiboException{
		// TODO Auto-generated method stub
		List<User> friendsList = null;
		UserWapper friendsWapper = null;
		try {
			friendsWapper = mWeibo.getFriendsWapper(mContext, userId, 0);
			friendsList = friendsWapper.getUsers();
			if (nextCursor == 0) {
				nextCursor = friendsWapper.getNextCursor();
			}
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			for (User friend : friendsList) {		
				temp.add(getMapFromUser(friend));
			}
			return temp;
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	@Override
	protected List<Map<String, Object>> getFooter() throws WeiboException{
		// TODO Auto-generated method stub
		List<User> friendsList = null;
		UserWapper friendsWapper = null;
		try {
			if (nextCursor == 0) {
				return new ArrayList<Map<String,Object>>(0);
			} else {
				friendsWapper = mWeibo.getFriendsWapper(mContext, userId, nextCursor);
				friendsList = friendsWapper.getUsers();
				nextCursor = friendsWapper.getNextCursor();
				List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
				for (User friend : friendsList) {		
					temp.add(getMapFromUser(friend));
				}
				return temp;
			}
			
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	@Override
	protected void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
	
	private void init(Context context) {
		this.mContext = context;
		Settings mSettings = new Settings(mContext);
		String token = mSettings.getToken();
		String tokenSecret = mSettings.getTokenSecret();	
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		mWeibo = Weibo.getInstance();
		mWeibo.setAccessToken(accessToken);
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}

}
