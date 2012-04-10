package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Status;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.googlecode.WeiboExtension.EventStream.Database;
import com.googlecode.WeiboExtension.EventStream.EventstreamSnsEngine;
import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.View.AbsWeiboList;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

public class MyMentionsListView extends AbsWeiboList{
	
	private static final String TAG = "MyMentionsListView";

	private Context mContext;
	private Weibo mWeibo;

	public MyMentionsListView(Context context) {
		super(context);
		init(context);
	}

	public MyMentionsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyMentionsListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@Override
	protected void pause() {
		// TODO Auto-generated method stub
		super.pause();
	}

	@Override
	protected void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
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
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

	@Override
	protected List<Map<String, Object>> getHeader() throws WeiboException{
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
		List<Status> myMentions = null;
		try {
			if (sinceId != 0)
				myMentions = mWeibo.getMentions(mContext, sinceId);
			else 
				myMentions = mWeibo.getMentions(mContext);
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();			
			TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(mContext);
			for (Status timeLine : myMentions) {		
				temp.add(getMapFromStatus(timeLine));
			//将该条微博存到数据库中
				dbAdapter.insertMyMention(timeLine);
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
		List<Map<String, Object>> weiboList = getWeiboList();
		List<Status> myMentions = null;
		try {
			long maxID = (Long) weiboList.get(weiboList.size() - 1).get(WeiboListAdapter.WEIBOLIST_ID);
			myMentions = mWeibo.getMentionsBefore(mContext, maxID - 1);
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			for (Status timeLine : myMentions) {		
				temp.add(getMapFromStatus(timeLine));
			}
			return temp;
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	private void init(Context context) {
		this.mContext = context;
		Settings mSettings = new Settings(mContext);
		String token = mSettings.getToken();
		String tokenSecret = mSettings.getTokenSecret();	
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		mWeibo = Weibo.getInstance();
		mWeibo.setAccessToken(accessToken);
		super.setWeiboList(super.restoreListFromDb());
	}
	
	@Override
	protected Cursor restoreCursorFromDb() {
		// TODO Auto-generated method stub
		TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(mContext);
		Cursor cursor = dbAdapter.queryMyMentions();
		return cursor;
	}

}

