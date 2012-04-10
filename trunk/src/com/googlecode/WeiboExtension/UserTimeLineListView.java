package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.View.AbsWeiboList;

public class UserTimeLineListView extends AbsWeiboList{
	
	private static final String TAG = "FriendsTimeLineListView";

	private Context mContext;
	private Weibo mWeibo;
	private long userId;

	public UserTimeLineListView(Context context) {
		super(context);
		init(context);
	}

	public UserTimeLineListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public UserTimeLineListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		super.onRefresh();
	}
	
	@Override
	protected void clearList() {
		// TODO Auto-generated method stub
		super.clearList();
	}

	@Override
	protected List<Map<String, Object>> getHeader() {
		// TODO Auto-generated method stub
		long sinceId;
		List<Map<String, Object>> weiboList = getWeiboList();
		if (weiboList == null || weiboList.size() == 0)
			sinceId = 0;
		else if (weiboList.size() == 1) {
			sinceId = (Long) weiboList.get(0).get(WeiboListAdapter.WEIBOLIST_ID);
		}else {
			sinceId = (Long) weiboList.get(1).get(WeiboListAdapter.WEIBOLIST_ID);
		}
		List<sinaweibo4android.api.Status> userTimeLine = null;
		try {
			if (sinceId != 0)
				userTimeLine = mWeibo.getUserTimeline(mContext, userId, sinceId);
			else 
				userTimeLine = mWeibo.getUserTimeline(mContext, userId);
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (userTimeLine != null) {
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			for (sinaweibo4android.api.Status timeLine : userTimeLine) {		
				temp.add(getMapFromStatus(timeLine));
			}
			return temp;
		}else {
			return null;
		}
	}
	
	@Override
	protected List<Map<String, Object>> getFooter() {
		// TODO Auto-generated method stub
		List<Map<String, Object>> weiboList = getWeiboList();
		List<sinaweibo4android.api.Status> userTimeLine = null;
		try {
			long maxID = (Long) weiboList.get(weiboList.size() - 1).get(WeiboListAdapter.WEIBOLIST_ID);
			userTimeLine = mWeibo.getUserTimelineBefore(mContext, userId, maxID);
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "FooterTask:WeiboException:" + e.getMessage());
			e.printStackTrace();
		}
		if (userTimeLine != null) {
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			for (sinaweibo4android.api.Status timeLine : userTimeLine) {		
				temp.add(getMapFromStatus(timeLine));
			}
			return temp;
		}else {
			return null;
		}
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
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
	
	@Override
	protected Cursor restoreCursorFromDb() {
		// TODO Auto-generated method stub
		return null;
	}

	
}

