package com.googlecode.WeiboExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader.ImageCallBack;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeiboListAdapter extends BaseAdapter{
	
	private static final String		TAG					= "WeiboListAdapter";
	
	public static final String 		WEIBOLIST_USER_ID 	= "user_id";
	public static final String 		WEIBOLIST_ID 		= "weibo_id";
	public static final String 		WEIBOLIST_LOGO_URL 	= "logo_url";
	public static final String 		WEIBOLIST_SCREEN_NAME = "screen_name";
	public static final String 		WEIBOLIST_CREATE_AT = "create_time";
	public static final String 		WEIBOLIST_THUMBNAIL_URL = "image_thumbnail_url";
	public static final String 		WEIBOLIST_BMIDDLE_URL = "image_bmiddle_url";
	public static final String 		WEIBOLIST_ORIGINAL_URL = "image_original_url";
	public static final String 		WEIBOLIST_TEXT = "text";
	public static final String 		WEIBOLIST_RETWEET_USER_ID = "retweet_user_id";
	public static final String		WEIBOLIST_RETWEET_SCREEN_NAME = "retweet_screen_name";
	public static final String		WEIBOLIST_RETWEET_TEXT = "retweet_text";
	
	public static final int 		LOGO_RES			= R.drawable.weibo_listview_avatar;
	public static final int 		IMAGE_RES			= R.drawable.weibo_listview_pic_loading;
	
	private Context					mContext;
	private LayoutInflater			mInflater;
	private List<Map<String, Object>> mWeiboList;
	
	public WeiboListAdapter(Context context, List<Map<String, Object>> timeLine) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mWeiboList = timeLine;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return mWeiboList.size();
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
		WeiboList weiboList = null;
		if (converView == null) {
			weiboList = new WeiboList();
			converView = mInflater.inflate(R.layout.weibo_listview_item, null);
			weiboList.logo = (ImageView)converView.findViewById(R.id.weibo_listview_item_logo);
			weiboList.screenName = (TextView)converView.findViewById(R.id.weibo_listview_item_screen_name);
			weiboList.createTime = (TextView)converView.findViewById(R.id.weibo_listview_item_create_time);
			weiboList.image = (ImageView)converView.findViewById(R.id.weibo_listview_item_image);
			weiboList.text = (TextView)converView.findViewById(R.id.weibo_listview_item_text);
			weiboList.retweetText = (TextView)converView.findViewById(R.id.weibo_listview_item_retweet_text);
			converView.setTag(weiboList);
		}else {
			weiboList = (WeiboList) converView.getTag();
		}
	//加载用户头像
		String logoURL = (String) mWeiboList.get(position).get(WEIBOLIST_LOGO_URL);
		Bitmap logoBitmap = new AsyncBitmapLoader(mContext).loadBitmap(weiboList.image, logoURL, 
				new ImageCallBack() {
					
					public void imageLoad(ImageView imageView, Bitmap bitmap) {
						// TODO Auto-generated method stub
						imageView.setImageBitmap(bitmap);
					}
				});
		if (logoBitmap == null) {
			weiboList.logo.setImageResource(LOGO_RES);
		}else {
			weiboList.logo.setImageBitmap(logoBitmap);	
		}
		weiboList.logo.setOnClickListener(new OnUserClickListener(mContext, 
				(Long) mWeiboList.get(position).get(WEIBOLIST_USER_ID),
				(String) mWeiboList.get(position).get(WEIBOLIST_LOGO_URL),
				(String) mWeiboList.get(position).get(WEIBOLIST_SCREEN_NAME)));
		
		weiboList.screenName.setText((String) mWeiboList.get(position).get(WEIBOLIST_SCREEN_NAME));
	//加载图片	
		String thumbnailPicUrl = (String) mWeiboList.get(position).get(WEIBOLIST_THUMBNAIL_URL);
		if (thumbnailPicUrl != null && thumbnailPicUrl.length() != 0) {
			weiboList.image.setVisibility(View.VISIBLE);
			Bitmap thumbnialBitmap = new AsyncBitmapLoader(mContext).loadBitmap(weiboList.image, thumbnailPicUrl, 
					new ImageCallBack() {
						
						public void imageLoad(ImageView imageView, Bitmap bitmap) {
							// TODO Auto-generated method stub
							imageView.setImageBitmap(bitmap);
						}
					});
			if (thumbnialBitmap == null) {
				weiboList.image.setImageResource(IMAGE_RES);
			}else {
				weiboList.image.setImageBitmap(thumbnialBitmap);	
			}
			String imageUrl = (String) mWeiboList.get(position).get(WEIBOLIST_BMIDDLE_URL);
			weiboList.image.setOnClickListener(new OnImageClickListener(mContext, imageUrl));
			
		}else {
			weiboList.image.setVisibility(View.INVISIBLE);
		}
		Date createAt = ((Date) mWeiboList.get(position).get(WEIBOLIST_CREATE_AT));
		String createTrack = Utility.getDateFormat(mContext, createAt);
		weiboList.createTime.setText(createTrack);
		weiboList.text.setText((String) mWeiboList.get(position).get(WEIBOLIST_TEXT));
		String retweetText = (String) mWeiboList.get(position).get(WEIBOLIST_RETWEET_TEXT);
		if (Constants.Config.DEBUG) {
			Log.d(TAG, retweetText + "");
		}
		if (retweetText != null) {
			String retweetScreenName = (String) mWeiboList.get(position).get(WEIBOLIST_RETWEET_SCREEN_NAME);
			SpannableString sp = new SpannableString(retweetScreenName + ":" + retweetText);
			sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
					R.color.cl_weibolist_retweet_screen_name)), 
					0, retweetScreenName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
					R.color.cl_weibolist_create_time)), 
					retweetScreenName.length() + 1, retweetScreenName.length() + retweetText.length() + 1,
					Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			weiboList.retweetText.setText(sp);
			weiboList.retweetText.setVisibility(View.VISIBLE);
		}else {
			//没有转发其他的内容，则retweet内容为空，若为View.INVISIBLE，则内容不显示，但仍占用空间
			weiboList.retweetText.setVisibility(View.GONE);
		}
		
		return converView;
	} 
	
	public final class WeiboList {
		ImageView logo;
		TextView screenName;
		TextView createTime;
		ImageView image;
		TextView text;
		TextView retweetText;
	}
	/**
	 * 图片按下监听器，监听微博列表中图片按下，然后通过WebImageViewer打开该图像
	 * @author yejinbing
	 *
	 */
	private class OnImageClickListener implements OnClickListener {
		private Context context;
		private String imageUrl;
		
		public OnImageClickListener(Context context, String imageUrl) {
			this.context = context;
			this.imageUrl = imageUrl;
		}
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			Intent intent = new Intent(context, WebImageViewer.class);
//			intent.putExtra("image_url", imageUrl);
//			context.startActivity(intent);
			WebImagePreview imagePreview = new WebImagePreview(v, imageUrl);
			imagePreview.show();
		}
		
	}
	
	private class OnUserClickListener implements OnClickListener {
		private Context context;
		private long userId;
		private String avatarUrl;
		private String userName;
		
		public OnUserClickListener(Context context, long userId, String avatarUrl, String userName) {
			this.context = context;
			this.userId = userId;
			this.avatarUrl = avatarUrl;
			this.userName = userName;
		}
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(context, UserProfileActivity.class);
			intent.putExtra(UserProfileActivity.EXTRA_USER_ID, userId);
			intent.putExtra(UserProfileActivity.EXTRA_AVATAR_URL, avatarUrl);
			intent.putExtra(UserProfileActivity.EXTRA_SCREEN_NAME, userName);
			context.startActivity(intent);
		}
	}

}
