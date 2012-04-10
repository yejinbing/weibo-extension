package com.googlecode.WeiboExtension;

import java.util.List;
import java.util.Map;
import com.googlecode.WeiboExtension.db.AccountDBAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountListAdapter extends BaseAdapter{
	
	public static final String USERLIST_LOGO = "userlist_logo";
	public static final String USERLIST_ID = "userlist_id";
	public static final String USERLIST_NAME = "userlist_name";
	public static final String USERLIST_DEFAULT = "userlist_default";
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mUserList;
	
	public AccountListAdapter(Context context, List<Map<String, Object>> userList) {
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
		UserListInfo userListInfo = null;
		if (converView == null) {
			userListInfo =  new UserListInfo();
			converView = mInflater.inflate(R.layout.userlist_item, null);
			userListInfo.logo = (ImageView)converView.findViewById(R.id.userlist_logo);
			userListInfo.id = (TextView)converView.findViewById(R.id.userlist_id);
			userListInfo.name = (TextView)converView.findViewById(R.id.userlist_name);
			userListInfo.defaultMark = (TextView)converView.findViewById(R.id.userlist_default);
			converView.setTag(userListInfo);
		}else {
			userListInfo = (UserListInfo) converView.getTag();
		}
		int mark = (Integer) mUserList.get(position).get(USERLIST_LOGO);
		switch (mark) {
			case AccountDBAdapter.WEIBO_MARK_SINA:
				userListInfo.logo.setImageResource(R.drawable.sina_icon);
				break;
			default:
				break;
		}
		userListInfo.id.setText(mUserList.get(position).get(USERLIST_ID).toString());
		userListInfo.name.setText((String) mUserList.get(position).get(USERLIST_NAME));
		if ((Boolean) mUserList.get(position).get(USERLIST_DEFAULT)) {
			userListInfo.defaultMark.setText(R.string.default_account);
			userListInfo.defaultMark.setVisibility(View.VISIBLE);
		}else {
			userListInfo.defaultMark.setVisibility(View.GONE);
		}
		return converView;
	}

	public class UserListInfo {
		ImageView logo;
		TextView id;
		TextView name;
		TextView defaultMark;
	}
}
