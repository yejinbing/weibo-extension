package com.googlecode.WeiboExtension.View;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Status;
import sinaweibo4android.api.User;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.UserListAdapter;
import com.googlecode.WeiboExtension.UserProfileActivity;
import com.googlecode.WeiboExtension.Utility.Utility;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class AbsUserList extends PullToRefreshListView{
	
	private static final String TAG = "AbsUserList";
	
	private Context mContext;
	private UserListAdapter userListAdapter;
	private List<Map<String, Object>> userList;
	private List<Map<String, Object>> userListFooter;
	
	private RefreshHeaderTask mRefreshHeaderTask;
	private RefreshFooterTask mRefreshFooterTask;
	
	private boolean isBottom = false;
	
	
	public AbsUserList(Context context) {
		super(context);
		init(context);
	}

	public AbsUserList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AbsUserList(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
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
//		this.setFadingEdgeLength(0);
//		this.setDivider(null);
//		this.setCacheColorHint(0);
//		this.setBackgroundResource(R.drawable.weibo_listview_bg);
		this.setSelector(R.drawable.weibo_listview_selector_bg);
		userList = new ArrayList<Map<String,Object>>();
		userListFooter = new ArrayList<Map<String,Object>>();
		userListAdapter = new UserListAdapter(mContext, userList);
		this.setAdapter(userListAdapter);
		this.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Map<String, Object> map = userList.get(position - 1);
				Intent intent = new Intent(mContext, UserProfileActivity.class);
				intent.putExtra(UserProfileActivity.EXTRA_USER_ID, 
						(Long) map.get(UserListAdapter.USERLIST_USER_ID));
				intent.putExtra(UserProfileActivity.EXTRA_AVATAR_URL, 
						(String) map.get(UserListAdapter.USERLIST_AVATAR_URL));
				intent.putExtra(UserProfileActivity.EXTRA_SCREEN_NAME, 
						(String) map.get(UserListAdapter.USERLIST_NAME));
				mContext.startActivity(intent);
			}
		});
		
	}
	//获取当前的微博列表
	public List<Map<String, Object>> getuserList() {
		return userList;
	}
	
	public List<Map<String, Object>> getuserListFooter() {
		return userListFooter;
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
				userList.addAll(0, result);
				userListAdapter.notifyDataSetChanged();
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
				if (result.size() == 0) {
					isBottom = true;
					removeLoadView();
				}else {
					userList.addAll(result);
				}	
				userListAdapter.notifyDataSetChanged();
				
			}else {
				
			}
			onRefreshFooterComplete();
			super.onPostExecute(result);
		}

	}
	
	protected abstract List<Map<String, Object>> getHeader() throws WeiboException;
	
	protected abstract List<Map<String, Object>> getFooter() throws WeiboException;
	
	protected void clearList() {
		userList.clear();
		userListFooter.clear();
		userListAdapter.notifyDataSetChanged();
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
	
	protected Map<String, Object> getMapFromUser(User user) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(UserListAdapter.USERLIST_USER_ID, user.getId());
		map.put(UserListAdapter.USERLIST_AVATAR_URL, user.getProfileImageURL().toString());
		map.put(UserListAdapter.USERLIST_NAME, user.getScreenName());
		map.put(UserListAdapter.USERLIST_INTRODUCE, user.getDescription());
		Status lastWeibo = user.getStatus();
		if (lastWeibo != null) {
			map.put(UserListAdapter.USERLIST_LATE_WEIBO, user.getStatus().getText());
		}
		map.put(UserListAdapter.USERlIST_IS_FOLLOWING, user.isFollowing());
		return map;
	}
}
