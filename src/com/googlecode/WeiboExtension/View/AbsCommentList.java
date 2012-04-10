package com.googlecode.WeiboExtension.View;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Comment;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.CommentAdapter;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;

public abstract class AbsCommentList extends PullToRefreshListView{
	
	private static final String TAG = "AbscommentList";
	
	private Context mContext;
	private CommentAdapter commentAdapter;
	private List<Map<String, Object>> commentList;
	private List<Map<String, Object>> commentListFooter;
	
	private RefreshHeaderTask mRefreshHeaderTask;
	private RefreshFooterTask mRefreshFooterTask;
	
	private boolean isBottom = false;
	
	
	public AbsCommentList(Context context) {
		super(context);
		init(context);
	}

	public AbsCommentList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AbsCommentList(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (mRefreshHeaderTask != null 
				&& mRefreshHeaderTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshHeaderTask.cancel(true);
		}
		mRefreshHeaderTask = new RefreshHeaderTask();
		mRefreshHeaderTask.execute();
		super.onRefresh();		
	}

	@Override
	protected void removeLoadView() {
		// TODO Auto-generated method stub
		super.removeLoadView();
	}

	@Override
	public void onRefreshComplete(CharSequence lastUpdated) {
		// TODO Auto-generated method stub
		super.onRefreshComplete(lastUpdated);
	}

	@Override
	public void onRefreshComplete() {
		// TODO Auto-generated method stub
		super.onRefreshComplete();
	}

	@Override
	public void onRefreshFooterComplete() {
		// TODO Auto-generated method stub
		super.onRefreshFooterComplete();
	}

	@Override
	public void onRefreshFooter() {
		// TODO Auto-generated method stub
		if (mRefreshFooterTask != null 
				&& mRefreshFooterTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshFooterTask.cancel(true);
		}
		mRefreshFooterTask = new RefreshFooterTask();
		mRefreshFooterTask.execute();
		super.onRefreshFooter();	
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if (!isBottom && view.getLastVisiblePosition() == (view.getCount() - 1)) {
			//不是底端	
				this.onRefreshFooter();
			}
		}
		super.onScrollStateChanged(view, scrollState);
	}	
	
	private void init(Context context) {
		this.mContext = context;
		this.setFadingEdgeLength(0);
		this.setDivider(context.getResources().getDrawable(R.drawable.commons_divider));
		this.setCacheColorHint(0);
		this.setBackgroundResource(R.drawable.singleweibo_bg);
		this.setSelector(R.drawable.weibo_listview_selector_bg);
		commentList = new ArrayList<Map<String,Object>>();
		commentListFooter = new ArrayList<Map<String,Object>>();
		commentAdapter = new CommentAdapter(mContext, commentList);
		this.setAdapter(commentAdapter);
	}
	
	protected void setCommentList(List<Map<String, Object>> list) {
		commentList.addAll(list);
		commentAdapter.notifyDataSetChanged();
	}
	
	protected void clearList() {
		commentList.clear();
		commentListFooter.clear();
		commentAdapter.notifyDataSetChanged();
	}
	
	//获取当前的微博列表
	public List<Map<String, Object>> getCommentList() {
		return commentList;
	}
	
	public List<Map<String, Object>> getCommentListFooter() {
		return commentListFooter;
	}
	
	private class RefreshHeaderTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected List<Map<String, Object>> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				List<Map<String, Object>> temp = getHeader();
				return temp;
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				if (e.getStatusCode() == 90000) {
					return new ArrayList<Map<String,Object>>(0);
				}else {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> result) {
			// TODO Auto-generated method stub
			if (result != null) {
				
//				commentListFooter.addAll(commentList);

				long sinceId = 0;
				if (commentList.size() != 0) {
					sinceId = (Long) commentList.get(0).get(CommentAdapter.COMMENT_ID);
				}
				int size = 0;
				for (Map<String, Object> map : result) {
					if ((Long) map.get(CommentAdapter.COMMENT_ID) > sinceId) {
						commentList.add(size++, map);
					}else {
						break;
					}
				}
				//中间有未刷新的，将之后的全部清除
				if (result.size() != 0 && size == result.size()) {
					int count = commentList.size() - size;
					while (count-- > 0) {
						commentList.remove(size);
					}
//					commentList.removeAll(commentListFooter);
				}
				
				Utility.displayToast(mContext, size + getResources().getString(R.string.new_microblogging));

				commentAdapter.notifyDataSetChanged();
			}else {
				Utility.displayToast(mContext, R.string.network_error);
			}
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			onRefreshComplete(getResources().getString(R.string.last_refresh_time)
					+ df.format(date));
			super.onPostExecute(result);
		}

	}
	
	private class RefreshFooterTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected List<Map<String, Object>> doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				List<Map<String, Object>> temp = getFooter();
				return temp;
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				if (e.getStatusCode() == 90000) {
					return new ArrayList<Map<String,Object>>(0);
				}else {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> result) {
			// TODO Auto-generated method stub
			if (result != null) {
				System.out.println("result,size=" + result.size());
				//已是最低端，无数据更新
				if (result.size() == 0) {
					isBottom = true;
					removeLoadView();
				}else {
					long maxId = (Long) commentList.get(commentList.size() - 1).get(CommentAdapter.COMMENT_ID);
					for (Map<String, Object> map : result) {
						long weiboId = (Long) map.get(CommentAdapter.COMMENT_ID);
						if (weiboId < maxId) {
							commentList.add(map);
						}
					}
					commentAdapter.notifyDataSetChanged();
				}
				
			}else {
				Utility.displayToast(mContext, R.string.network_error);
			}
			onRefreshFooterComplete();
			super.onPostExecute(result);
		}

	}
	
	protected abstract List<Map<String, Object>> getHeader() throws WeiboException;
	
	protected abstract List<Map<String, Object>> getFooter() throws WeiboException;

	protected void pause() {
		if (mRefreshHeaderTask != null 
				&& mRefreshHeaderTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshHeaderTask.cancel(true);
		}
		if (mRefreshFooterTask != null 
				&& mRefreshFooterTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshFooterTask.cancel(true);
		}
		
	}
	
	protected void destroy() {
		if (mRefreshHeaderTask != null && 
				mRefreshHeaderTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshHeaderTask.cancel(true);
		}
		
		if (mRefreshFooterTask != null && 
				mRefreshFooterTask.getStatus() != AsyncTask.Status.FINISHED) {
			mRefreshFooterTask.cancel(true);
		}
		
	}
	
	protected abstract Cursor restoreCursorFromDb();
	
	protected List<Map<String, Object>> restoreListFromDb() {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Cursor cursor = null;
		try {
			cursor = restoreCursorFromDb();
			while (cursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(CommentAdapter.COMMENT_ID, cursor.getLong(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_ID)));
				map.put(CommentAdapter.COMMENT_USER_ID, cursor.getLong(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_USER_ID)));
				map.put(CommentAdapter.COMMENT_LOGO_URL, cursor.getString(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_AVATAR_URL)));
				map.put(CommentAdapter.COMMENT_NAME, cursor.getString(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_NAME)));
				map.put(CommentAdapter.COMMENT_CREATE, new Date(cursor.getLong(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_CREATE))));
				map.put(CommentAdapter.COMMENT_TEXT, cursor.getString(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_TEXT)));
				map.put(CommentAdapter.COMMENT_REPLY_TEXT, cursor.getString(
						cursor.getColumnIndex(TimeLineDBAdapter.KEY_COMMENT_REPLY_TEXT)));
				list.add(map);
			}
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return list;
	}
	
	protected Map<String, Object> getMapFromComment(Comment comment) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(CommentAdapter.COMMENT_ID, comment.getId());
		map.put(CommentAdapter.COMMENT_USER_ID, comment.getUser().getId());
		map.put(CommentAdapter.COMMENT_LOGO_URL, comment.getUser().getProfileImageURL().toString());
		map.put(CommentAdapter.COMMENT_NAME, comment.getUser().getScreenName());
		map.put(CommentAdapter.COMMENT_CREATE, comment.getCreatedAt());
		map.put(CommentAdapter.COMMENT_TEXT, comment.getText());
		if (comment.getStatus() != null) {
			map.put(CommentAdapter.COMMENT_REPLY_TEXT, comment.getStatus().getText());
		}else {
			map.put(CommentAdapter.COMMENT_REPLY_TEXT, comment.getreplyComment());
		}
		
		return map;
	}
}
