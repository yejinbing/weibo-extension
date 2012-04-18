package com.googlecode.WeiboExtension.View;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Status;

import com.googlecode.WeiboExtension.AccountListActivity;
import com.googlecode.WeiboExtension.Constants;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.SingleWeiboActivity;
import com.googlecode.WeiboExtension.WeiboListAdapter;
import com.googlecode.WeiboExtension.EventStream.SNSSamplePluginConfig;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class AbsWeiboList extends PullToRefreshListView{
	
	private static final String TAG = "AbsWeiboList";
	
	private Context mContext;
	private WeiboListAdapter weiboListAdapter;
	private List<Map<String, Object>> weiboList;
	private List<Map<String, Object>> weiboListFooter;
	
	private RefreshHeaderTask mRefreshHeaderTask;
	private RefreshFooterTask mRefreshFooterTask;
	
	private boolean isBottom = false;
	
	public AbsWeiboList(Context context) {
		super(context);
		init(context);
	}

	public AbsWeiboList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AbsWeiboList(Context context, AttributeSet attrs,
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
			if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
			//不是底端	
				if (!isBottom) {
					this.onRefreshFooter();
				}
			}
		}
		super.onScrollStateChanged(view, scrollState);
	}	
	
	private void init(Context context) {
		this.mContext = context;
		this.setFadingEdgeLength(0);
		this.setDivider(null);
		this.setCacheColorHint(0);
		this.setBackgroundResource(R.drawable.weibo_listview_bg);
		this.setSelector(R.drawable.weibo_listview_selector_bg);
		weiboList = new ArrayList<Map<String,Object>>();
		weiboListFooter = new ArrayList<Map<String,Object>>();
		weiboListAdapter = new WeiboListAdapter(mContext, weiboList);
		this.setAdapter(weiboListAdapter);
		this.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, SingleWeiboActivity.class);
				Map<String, Object> map = weiboList.get(position - 1);
				intent.putExtra(SingleWeiboActivity.EXTRA_WEIBO_ID, 
						(Long) map.get(WeiboListAdapter.WEIBOLIST_ID));
				mContext.startActivity(intent);
			}
		});
		
	}
	
	protected void setWeiboList(List<Map<String, Object>> list) {
		weiboList.addAll(list);
		weiboListAdapter.notifyDataSetChanged();
	}
	
	//获取当前的微博列表
	protected List<Map<String, Object>> getWeiboList() {
		return weiboList;
	}
	
	protected void clearList() {
		weiboList.clear();
		weiboListAdapter.notifyDataSetChanged();
	}
	
	public List<Map<String, Object>> getWeiboListFooter() {
		return weiboListFooter;
	}	
	
	private class RefreshHeaderTask extends AsyncTask<Void, Void, List<Map<String, Object>>> {

		private int statusCode = -1;
		
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
				statusCode = e.getStatusCode();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Map<String, Object>> result) {
			// TODO Auto-generated method stub
			if (result != null) {
				
//				weiboListFooter.addAll(weiboList);

				long sinceId = 0;
				if (weiboList.size() != 0) {
					sinceId = (Long) weiboList.get(0).get(WeiboListAdapter.WEIBOLIST_ID);
				}
				int size = 0;
				for (Map<String, Object> map : result) {
					if ((Long) map.get(WeiboListAdapter.WEIBOLIST_ID) > sinceId) {
						weiboList.add(size++, map);
					}else {
						break;
					}
				}
				//中间有未刷新的，将之后的全部清除
				if (result.size() != 0 && size == result.size()) {
					int count = weiboList.size() - size;
					while (count-- > 0) {
						weiboList.remove(size);
					}
//					weiboList.removeAll(weiboListFooter);
				}
				
				Utility.displayToast(mContext, size + getResources().getString(R.string.new_microblogging));

				weiboListAdapter.notifyDataSetChanged();
			}else {
//				Utility.displayToast(mContext, R.string.network_error);
				switch (statusCode) {						
					case 21327:
						new AlertDialog.Builder(mContext)
								.setTitle(R.string.expired_token)
								.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										Intent intent = new Intent(mContext, AccountListActivity.class);
										intent.setAction(AccountListActivity.INTENT_ACTION_LOGIN);
										mContext.startActivity(intent);
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.show();
					case 90000:
					default:
				}
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
					long maxId = (Long) weiboList.get(weiboList.size() - 1).get(WeiboListAdapter.WEIBOLIST_ID);
					for (Map<String, Object> map : result) {
						long weiboId = (Long) map.get(WeiboListAdapter.WEIBOLIST_ID);
						if (weiboId < maxId) {
							weiboList.add(map);
						}
					}
					weiboListAdapter.notifyDataSetChanged();
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
	
	protected abstract Cursor restoreCursorFromDb();
	
	protected List<Map<String, Object>> restoreListFromDb() {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Cursor cursor = null;
		try {
			cursor = restoreCursorFromDb();
			while (cursor.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(WeiboListAdapter.WEIBOLIST_USER_ID, 
						cursor.getLong(cursor.getColumnIndex(TimeLineDBAdapter.KEY_USER_ID)));
				map.put(WeiboListAdapter.WEIBOLIST_ID, 
						cursor.getLong(cursor.getColumnIndex(TimeLineDBAdapter.KEY_ID)));
				map.put(WeiboListAdapter.WEIBOLIST_LOGO_URL, 
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_AVATAR_URL)));
				map.put(WeiboListAdapter.WEIBOLIST_SCREEN_NAME, 
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_SCREEN_NAME)));
				map.put(WeiboListAdapter.WEIBOLIST_CREATE_AT, 
						new Date(cursor.getLong(cursor.getColumnIndex(TimeLineDBAdapter.KEY_CREATE_TIME))));
				map.put(WeiboListAdapter.WEIBOLIST_TEXT, 
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_TEXT)));
				long retweetUserId = 
					cursor.getLong(cursor.getColumnIndex(TimeLineDBAdapter.KEY_RETWEET_USER_ID));
				if (retweetUserId != 0) {
					map.put(WeiboListAdapter.WEIBOLIST_RETWEET_USER_ID, retweetUserId);
					map.put(WeiboListAdapter.WEIBOLIST_RETWEET_SCREEN_NAME, 
							cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_RETWEET_SCREEN_NAME)));
					map.put(WeiboListAdapter.WEIBOLIST_RETWEET_TEXT, 
							cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_RETWEET_TEXT)));
				}
				map.put(WeiboListAdapter.WEIBOLIST_THUMBNAIL_URL,
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_THUMBNAIL_URL)));
				map.put(WeiboListAdapter.WEIBOLIST_BMIDDLE_URL, 
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_BMIDDLE_URL)));
				map.put(WeiboListAdapter.WEIBOLIST_ORIGINAL_URL,
						cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_ORIGINAL_URL)));
				list.add(map);
			}
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return list;
	}
	
	protected Map<String, Object> getMapFromStatus(Status status) {
		Map<String, Object> map = new HashMap<String, Object>();
		Log.d(TAG, "user id:" + status.getUser().getId());
		map.put(WeiboListAdapter.WEIBOLIST_USER_ID, status.getUser().getId());
		map.put(WeiboListAdapter.WEIBOLIST_ID, status.getId());
		map.put(WeiboListAdapter.WEIBOLIST_LOGO_URL, status.getUser().getProfileImageURL().toString());
		map.put(WeiboListAdapter.WEIBOLIST_SCREEN_NAME, status.getUser().getScreenName());
		map.put(WeiboListAdapter.WEIBOLIST_CREATE_AT, status.getCreatedAt());
		map.put(WeiboListAdapter.WEIBOLIST_TEXT, status.getText());
		Status retweetStatus = status.getRetweeted_status();
		if (retweetStatus != null) {
			map.put(WeiboListAdapter.WEIBOLIST_RETWEET_USER_ID, retweetStatus.getUser().getId());
			map.put(WeiboListAdapter.WEIBOLIST_RETWEET_SCREEN_NAME, "@" + retweetStatus.getUser().getName());
			map.put(WeiboListAdapter.WEIBOLIST_RETWEET_TEXT, retweetStatus.getText());
			
			String thumbnailPicUrl = retweetStatus.getThumbnail_pic();
			if (thumbnailPicUrl != null && thumbnailPicUrl.length() != 0) {
				map.put(WeiboListAdapter.WEIBOLIST_THUMBNAIL_URL, thumbnailPicUrl);
				map.put(WeiboListAdapter.WEIBOLIST_BMIDDLE_URL, retweetStatus.getBmiddle_pic());
				map.put(WeiboListAdapter.WEIBOLIST_ORIGINAL_URL, retweetStatus.getOriginal_pic());
			}
		}
		String thumbnailPicUrl = status.getThumbnail_pic();
		if (thumbnailPicUrl != null && thumbnailPicUrl.length() != 0) {
			if (Constants.Config.DEBUG) {
				Log.d(TAG, "thumbnailPicUrl:" + thumbnailPicUrl);
			}
			map.put(WeiboListAdapter.WEIBOLIST_THUMBNAIL_URL, thumbnailPicUrl);
			map.put(WeiboListAdapter.WEIBOLIST_BMIDDLE_URL, status.getBmiddle_pic());
			map.put(WeiboListAdapter.WEIBOLIST_ORIGINAL_URL, status.getOriginal_pic());
		}
		
		return map;
	}
	
	
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
}
