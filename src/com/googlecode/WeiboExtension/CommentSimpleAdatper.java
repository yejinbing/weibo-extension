package com.googlecode.WeiboExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.googlecode.WeiboExtension.Utility.Utility;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CommentSimpleAdatper extends BaseAdapter{
	
	public static final String COMMENT_ID = "comment_id";
	public static final String COMMENT_USER_ID = "comment_user_id";
	public static final String COMMENT_NAME = "comment_name";
	public static final String COMMENT_CREATE = "comment_create";
	public static final String COMMENT_TEXT = "comment_text";
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mComments;
	
	public CommentSimpleAdatper(Context context, List<Map<String, Object>> comments) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mComments = comments;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mComments.size();
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
		Comment comment = null;
		if (converView == null) {
			comment = new Comment();
			converView = mInflater.inflate(R.layout.comment_simple_item, null);
			comment.tvName = (TextView) converView.findViewById(R.id.comment_simple_item_name);
			comment.tvCreate = (TextView)converView.findViewById(R.id.comment_simple_item_create);
			comment.tvText = (TextView)converView.findViewById(R.id.comment_simple_item_text);
			converView.setTag(comment);
		}else {
			comment = (Comment) converView.getTag();
		}
		comment.tvName.setText((String) mComments.get(position).get(COMMENT_NAME));
		Date createAt = ((Date) mComments.get(position).get(COMMENT_CREATE));
		String createTrack = Utility.getDateFormat(mContext, createAt);
		comment.tvCreate.setText(createTrack);
		comment.tvText.setText((String) mComments.get(position).get(COMMENT_TEXT));
		
		StateListDrawable listDrawable = new StateListDrawable();
		listDrawable.addState(
				new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, 
				mContext.getResources().getDrawable(R.drawable.comment_reply_pressed));
		listDrawable.addState(new int[] { android.R.attr.state_enabled },
				mContext.getResources().getDrawable(R.drawable.comment_reply_normal));
		
		return converView;
	}

	public final  class Comment {
		TextView tvName;
		TextView tvCreate;
		TextView tvText;
	}
}
