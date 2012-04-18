package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Comment;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.View.AbsCommentList;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

public class CommentsToMeListView extends AbsCommentList{
	
	private static final String		TAG					= "FriendsTimeLineListView";

	private Context					mContext;
	private Weibo					mWeibo;

	public CommentsToMeListView(Context context) {
		super(context);
		init(context);
	}

	public CommentsToMeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CommentsToMeListView(Context context, AttributeSet attrs,
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
		List<Map<String, Object>> commentList = getCommentList();
		if (commentList == null || commentList.size() == 0)
			sinceId = 0;
		else if (commentList.size() == 1) {
			sinceId = (Long) commentList.get(0).get(CommentAdapter.COMMENT_ID);
		}else {
			sinceId = (Long) commentList.get(1).get(CommentAdapter.COMMENT_ID);
		}
		List<Comment> comments = null;
		try {
			if (sinceId != 0)
				comments = mWeibo.getCommentsToMe(mContext, sinceId);
			else 
				comments = mWeibo.getCommentsToMe(mContext);
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(mContext);
			for (Comment comment : comments) {		
				temp.add(getMapFromComment(comment));
				dbAdapter.insertCommentToMe(comment);
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
		List<Map<String, Object>> commentList = getCommentList();
		List<Comment> comments = null;
		try {
			long maxID = (Long) commentList.get(commentList.size() - 1).get(CommentAdapter.COMMENT_ID);
			comments = mWeibo.getCommentsToMeBefore(mContext, maxID - 1);
			List<Map<String, Object>> temp = new ArrayList<Map<String,Object>>();
			for (Comment comment : comments) {		
				temp.add(getMapFromComment(comment));
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
		super.setCommentList(super.restoreListFromDb());
	}

	@Override
	protected Cursor restoreCursorFromDb() {
		// TODO Auto-generated method stub
		TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(mContext);
		Cursor cursor = dbAdapter.queryCommentsToMe();
		return cursor;
	}
	
}

