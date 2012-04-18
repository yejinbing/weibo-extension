package com.googlecode.WeiboExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader.ImageCallBack;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter{
	
	public static final String		COMMENT_ID			= "comment_id";
	public static final String		COMMENT_USER_ID		= "comment_user_id";
	public static final String		COMMENT_LOGO_URL	= "logo_url";
	public static final String		COMMENT_NAME		= "comment_name";
	public static final String		COMMENT_CREATE		= "comment_create";
	public static final String		COMMENT_TEXT		= "comment_text";
	public static final String		COMMENT_REPLY_TEXT	= "reply_text";
	
	private Context					mContext;
	private LayoutInflater			mInflater;
	private List<Map<String, Object>> mComments;
	
	public static final int			LOGO_RES			= R.drawable.weibo_listview_avatar;
	
	public CommentAdapter(Context context, List<Map<String, Object>> comments) {
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
			converView = mInflater.inflate(R.layout.comment_item, null);
			comment.ivLogo = (ImageView)converView.findViewById(R.id.comment_item_logo);
			comment.tvName = (TextView) converView.findViewById(R.id.comment_item_name);
			comment.tvCreate = (TextView)converView.findViewById(R.id.comment_item_create);
			comment.tvText = (TextView)converView.findViewById(R.id.comment_item_text);
			comment.tvReplyText = (TextView)converView.findViewById(R.id.comment_item_reply_text);
			converView.setTag(comment);
		}else {
			comment = (Comment) converView.getTag();
		}
		String logoURL = (String) mComments.get(position).get(COMMENT_LOGO_URL);
		Bitmap logoBitmap = new AsyncBitmapLoader(mContext).loadBitmap(comment.ivLogo, logoURL, 
				new ImageCallBack() {
					
					public void imageLoad(ImageView imageView, Bitmap bitmap) {
						// TODO Auto-generated method stub
						imageView.setImageBitmap(bitmap);
					}
				});
		if (logoBitmap == null) {
			comment.ivLogo.setImageResource(LOGO_RES);
		}else {
			comment.ivLogo.setImageBitmap(logoBitmap);	
		}
		
		comment.tvName.setText((String) mComments.get(position).get(COMMENT_NAME));
		Date createAt = ((Date) mComments.get(position).get(COMMENT_CREATE));
		String createTrack = Utility.getDateFormat(mContext, createAt);
		comment.tvCreate.setText(createTrack);
		comment.tvText.setText((String) mComments.get(position).get(COMMENT_TEXT));
		comment.tvReplyText.setText((String) mComments.get(position).get(COMMENT_REPLY_TEXT));
		
		return converView;
	}

	public final  class Comment {
		ImageView ivLogo;
		TextView tvName;
		TextView tvCreate;
		TextView tvText;
		TextView tvReplyText;
	}
}
