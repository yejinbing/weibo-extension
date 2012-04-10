package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Comment;
import sinaweibo4android.api.Favorites;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.View.ToolBarPanelView;
import com.googlecode.WeiboExtension.View.ToolBarView;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader;
import com.googlecode.WeiboExtension.net.AsyncBitmapLoader.ImageCallBack;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SingleWeiboActivity extends Activity{
	
	private static final String TAG = "SingleWeiboActivity";
	
	private Context currentContext = SingleWeiboActivity.this;
	
	public static final String EXTRA_WEIBO_ID = "weibo_id";
	public static final String EXTRA_ACCESS_TOKEN = "accesstoken";
    public static final String EXTRA_TOKEN_SECRET = "consumerkey";

	private List<Map<String, Object>> mComments;
	private CommentSimpleAdatper mCommentAdatper;
	
	private LinearLayout weiboView;
	private ListView mSingleWeiboListView;
	private LinearLayout mLoadView;
	private ProgressBar mLoadViewProgress;
	private TextView mLoadViewText;
	
	private ImageView weiboLogo;
	private TextView weiboScreenName;
	private TextView weiboCreateAt;
	private LinearLayout weiboContent;
	private TextView weiboText;
	private ImageView weiboImage;
	private TextView weiboRepostComment;
	private LinearLayout weiboRetweetContent;
	private TextView weiboRetweetText;
	private ImageView weiboRetweetImage;
	private TextView weiboRetweetCreateAt;
	private TextView weiboRetweetRepostComment;
	
	private String imageUrl;
	private String retweetImageUrl;
	
	private long weiboId;
	private Weibo mWeibo;
	
	private ToolBarPanelView mToolBarPanel;
	private ToolBarView toolBack;
	private ToolBarView toolFavorite;
	private ToolBarView toolComment;
	private ToolBarView toolTransmit;
	private ToolBarView toolShare;
	
	private boolean isBottom = false;	//判断是否处于底端
		
	public static final int LOGO_RES = R.drawable.avatar;
	public static final int IMAGE_RES = R.drawable.weibo_listview_pic_loading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleweibo_view);
		
		mSingleWeiboListView = (ListView)findViewById(R.id.singleweibo_list);
		
		mToolBarPanel = (ToolBarPanelView)findViewById(R.id.singleweibo_toolbarpanel);
		
		initToolBar();
		
		LayoutInflater inflater = (LayoutInflater) currentContext.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		weiboView = (LinearLayout) inflater.inflate(R.layout.singleweibo, null);
		
		weiboLogo = (ImageView)weiboView.findViewById(R.id.singleweibo_logo);
		weiboScreenName = (TextView)weiboView.findViewById(R.id.singleweibo_screen_name);
		weiboCreateAt = (TextView)weiboView.findViewById(R.id.singleweibo_create_at);
		weiboContent = (LinearLayout)weiboView.findViewById(R.id.singleweibo_content);
		weiboText = (TextView)weiboView.findViewById(R.id.singleweibo_text);
		weiboImage = (ImageView)weiboView.findViewById(R.id.singleweibo_image);
		weiboRepostComment = (TextView)weiboView.findViewById(R.id.singleweibo_repost_comment);
		weiboRetweetContent = (LinearLayout)weiboView.findViewById(R.id.singleweibo_retweet_content);
		weiboRetweetText = (TextView)weiboView.findViewById(R.id.singleweibo_retweet_text);
		weiboRetweetImage = (ImageView)weiboView.findViewById(R.id.singleweibo_retweet_image);
		weiboRetweetCreateAt = (TextView)weiboView.findViewById(R.id.singleweibo_retweet_create);
		weiboRetweetRepostComment = (TextView)weiboView.findViewById(R.id.singleweibo_retweet_repost_comment);

		Settings mSettings = new Settings(currentContext);
		String token = mSettings.getToken();
		String tokenSecret = mSettings.getTokenSecret();	
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		mWeibo = Weibo.getInstance();
		mWeibo.setAccessToken(accessToken);
		
		Intent intent = this.getIntent();
		weiboId = intent.getLongExtra(EXTRA_WEIBO_ID, 0);
		
		Log.d(TAG, "weibo id:" + weiboId);
		if (weiboId != 0) {
			TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(this);
	        Cursor cursor = null;
	        try {
	        	cursor = dbAdapter.queryStatus(weiboId);
	        	if (cursor.moveToNext()) {
	        		String logoURL = (String)cursor.getString(cursor.getColumnIndex(TimeLineDBAdapter.KEY_AVATAR_URL));
					Bitmap logoBitmap = new AsyncBitmapLoader(currentContext).loadBitmap(weiboLogo, logoURL, 
							new ImageCallBack() {
								
								public void imageLoad(ImageView imageView, Bitmap bitmap) {
									// TODO Auto-generated method stub
									imageView.setImageBitmap(bitmap);
								}
							});
					if (logoBitmap == null) {
						weiboLogo.setImageResource(LOGO_RES);
					}else {
						weiboLogo.setImageBitmap(logoBitmap);	
					}					
					weiboScreenName.setText(cursor.getString(
							cursor.getColumnIndex(TimeLineDBAdapter.KEY_SCREEN_NAME)));
					Date createAt = new Date(cursor.getLong(
							cursor.getColumnIndex(TimeLineDBAdapter.KEY_CREATE_TIME)));
					String createTrack = Utility.getDateFormat(currentContext, createAt);
					weiboCreateAt.setText(createTrack);
					weiboText.setText(cursor.getString(
							cursor.getColumnIndex(TimeLineDBAdapter.KEY_TEXT)));
					String retweetScreenName = cursor.getString(
							cursor.getColumnIndex(TimeLineDBAdapter.KEY_RETWEET_SCREEN_NAME));
					if (retweetScreenName != null) {
						String retweetText = cursor.getString(
								cursor.getColumnIndex(TimeLineDBAdapter.KEY_RETWEET_TEXT));
						SpannableString sp = new SpannableString(retweetScreenName + ":" + retweetText);
						sp.setSpan(new ForegroundColorSpan(currentContext.getResources().getColor(
								R.color.cl_timeline_retweet_screen_name)), 
								0, retweetScreenName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						sp.setSpan(new ForegroundColorSpan(currentContext.getResources().getColor(
								R.color.cl_timeline_retweet_text)), 
								retweetScreenName.length(), retweetScreenName.length() + retweetText.length(),
								Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						weiboRetweetText.setText(sp);
						weiboRetweetContent.setVisibility(View.VISIBLE);
					}else {
						weiboRetweetContent.setVisibility(View.GONE);
					}
					
	        	}        	
	        }finally {
	        	if (cursor != null) {
	        		cursor.close();
	        	}
	        }
		}
		
		mSingleWeiboListView.addHeaderView(weiboView);
		
		mLoadView = (LinearLayout) inflater.inflate(R.layout.pull_to_load_footer, null);
		mLoadViewProgress = (ProgressBar) mLoadView
				.findViewById(R.id.pull_to_load_progress);
		mLoadViewText = (TextView) mLoadView
				.findViewById(R.id.pull_to_load_text);
		mSingleWeiboListView.addFooterView(mLoadView);
		mLoadView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				// TODO Auto-generated method stub
				refreshComments();
			}
		});
		
		
		mComments = new ArrayList<Map<String,Object>>();
		mCommentAdatper = new CommentSimpleAdatper(currentContext, mComments);
		mSingleWeiboListView.setAdapter(mCommentAdatper);
		
		mSingleWeiboListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				//监听滚动到最底部时，刷新之前较老的评论
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (!isBottom && view.getLastVisiblePosition() == (view.getCount() - 1)) {
						refreshComments();
					}
				}
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem, 
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
		weiboImage.setOnClickListener(new OnImageClickListener());		
		weiboRetweetImage.setOnClickListener(new OnImageClickListener());
		
		refreshWeiboInfo();
		
		refreshComments();
		
	}
	
	private void initToolBar() {
		toolBack = new ToolBarView(currentContext, R.string.back,
				R.drawable.singleweibo_back_normal, -1);
		mToolBarPanel.addTool(toolBack);
		toolFavorite = new ToolBarView(currentContext, R.string.favorite,
				R.drawable.singleweibo_favorite, R.drawable.singleweibo_favorite_on);
		mToolBarPanel.addTool(toolFavorite);
		toolComment = new ToolBarView(currentContext, R.string.comment,
				R.drawable.singleweibo_comment, -1);
		mToolBarPanel.addTool(toolComment);
		toolTransmit = new ToolBarView(currentContext, R.string.transmit,
				R.drawable.singleweibo_transmit, -1);
		mToolBarPanel.addTool(toolTransmit);
		toolShare = new ToolBarView(currentContext, R.string.share,
				R.drawable.singleweibo_share, -1);
		mToolBarPanel.addTool(toolShare);
		
		toolBack.setOnClickListener(new OnBackClickListener());
		toolFavorite.setOnClickListener(new OnFavoriteClickListener());
		toolComment.setOnClickListener(new OnCommentClickListener());
		toolTransmit.setOnClickListener(new OnTransmitClickListener());
		toolShare.setOnClickListener(new OnShareClickListener());
	}

	private void refreshComments() {
		if (mComments == null) {
			Log.e(TAG, "refreshComments:mComments is null");
			return;			
		}
		new FooterTask().execute();
	}
	
	private class FooterTask extends AsyncTask<Void, Void,List<Comment>> {

		@Override
		protected void onPreExecute() {
			mLoadViewProgress.setVisibility(View.VISIBLE);
			mLoadViewText.setText(R.string.pull_to_refresh_refreshing_label);
			super.onPreExecute();
		}

		@Override
		protected List<Comment> doInBackground(Void... params) {
			// Simulates a background job.
			List<Comment> comments = null;
			try {
				if (mComments.size() == 0) {
					comments = mWeibo.getComments(currentContext, weiboId);
				}else {
					long maxId = (Long) mComments.get(mComments.size() - 1).get(CommentSimpleAdatper.COMMENT_ID);
					comments = mWeibo.getComments(currentContext, weiboId, maxId - 1);
				}
				return comments;	
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				return new ArrayList<Comment>(0);
			}
			
		}

		@Override
		protected void onPostExecute(List<Comment> result) {

			mLoadViewProgress.setVisibility(View.INVISIBLE);
			mLoadViewText.setText(R.string.pull_to_refresh_tap_label);
			
			if (result != null) {
				if (result.size() == 0) {
					mSingleWeiboListView.removeFooterView(mLoadView);
				}else {
					for (Comment comment : result) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(CommentSimpleAdatper.COMMENT_ID, comment.getId());
						map.put(CommentSimpleAdatper.COMMENT_USER_ID, comment.getUser().getId());
						map.put(CommentSimpleAdatper.COMMENT_NAME, comment.getUser().getScreenName());
						map.put(CommentSimpleAdatper.COMMENT_CREATE, comment.getCreatedAt());
						map.put(CommentSimpleAdatper.COMMENT_TEXT, comment.getText());
						
						mComments.add(map);
					}
				}
				
			}else {
				Utility.displayToast(currentContext, R.string.network_error);
			}
			
			mCommentAdatper.notifyDataSetChanged();

			super.onPostExecute(result);
		}
	}
	
	private void refreshWeiboInfo() {
		new refreshWeiboTask().execute();
	}
	
	private class refreshWeiboTask extends AsyncTask<Void, Void, sinaweibo4android.api.Status> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}

		@Override
		protected sinaweibo4android.api.Status doInBackground(Void... params) {
			// Simulates a background job.
			sinaweibo4android.api.Status status = null;
			try {
				status = mWeibo.showStatus(currentContext, weiboId);
				return status;
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				return null;
			}
			
		}

		@Override
		protected void onPostExecute(sinaweibo4android.api.Status result) {
			
			if (result != null) {
				String logoURL = result.getUser().getProfileImageURL().toString();
				Bitmap logoBitmap = new AsyncBitmapLoader(currentContext).loadBitmap(weiboLogo, logoURL, 
						new ImageCallBack() {
							
							public void imageLoad(ImageView imageView, Bitmap bitmap) {
								// TODO Auto-generated method stub
								imageView.setImageBitmap(bitmap);
							}
						});
				if (logoBitmap == null) {
					weiboLogo.setImageResource(LOGO_RES);
				}else {
					weiboLogo.setImageBitmap(logoBitmap);	
				}					
				weiboScreenName.setText(result.getUser().getScreenName());
				weiboText.setText(result.getText());
				//微博的发表时间
				String createTrack = Utility.getDateFormat(currentContext, result.getCreatedAt());
				weiboCreateAt.setText(createTrack);
				//微博的转发和评论数
				int repostsCount = result.getRepostsCount();
				int commentsCount = result.getCommentsCount();				
				String repostComment = currentContext.getResources().getString(R.string.reposts_count)
						+ "(" + repostsCount + ") | "
						+ currentContext.getResources().getString(R.string.comments_count)
						+ "(" + commentsCount + ")";
				weiboRepostComment.setText(repostComment);
				
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "reposts:" + repostsCount + " comments:" + commentsCount);
				}
				//显示图片
				String thumbnailUrl = result.getThumbnail_pic();
				if (thumbnailUrl != null && thumbnailUrl.length() != 0) {
					imageUrl = result.getOriginal_pic();
					weiboImage.setVisibility(View.VISIBLE);
					Bitmap thumbnialBitmap = new AsyncBitmapLoader(currentContext).
								loadBitmap(weiboImage, thumbnailUrl, 
							new ImageCallBack() {
								
								public void imageLoad(ImageView imageView, Bitmap bitmap) {
									// TODO Auto-generated method stub
									imageView.setImageBitmap(bitmap);
								}
							});
					if (thumbnialBitmap == null) {
						weiboImage.setImageResource(IMAGE_RES);
					}else {
						weiboImage.setImageBitmap(thumbnialBitmap);	
					}
					
				}else {
					weiboImage.setVisibility(View.GONE);
				}
			//设置微博收藏状态
				toolFavorite.setImageState(result.isFavorited());
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "refreshWeiboTask:isFavorited" + result.isFavorited());
				}
				
				sinaweibo4android.api.Status retweetStatus = result.getRetweeted_status();
				if (retweetStatus != null) {
					//转发原文的发表时间
					String retweetCreateTrack = Utility.getDateFormat(currentContext, retweetStatus.getCreatedAt());
					weiboRetweetCreateAt.setText(retweetCreateTrack);
					//转发原文的转发和评论数
					int retweetRepostsCount = result.getRetweeted_status().getRepostsCount();
					int retweetCommentsCount = result.getRetweeted_status().getCommentsCount();
					String retweetRepostComment = currentContext.getResources().getString(R.string.reposts_count)
							+ "(" + retweetRepostsCount + ") | "
							+ currentContext.getResources().getString(R.string.comments_count)
							+ "(" + retweetCommentsCount + ")";
					weiboRetweetRepostComment.setText(retweetRepostComment);
					
					String retweetThumbnailUrl = retweetStatus.getThumbnail_pic();
					if (retweetThumbnailUrl != null && retweetThumbnailUrl.length() != 0) {
						retweetImageUrl = retweetStatus.getOriginal_pic();
						weiboRetweetImage.setVisibility(View.VISIBLE);
						Bitmap thumbnialBitmap = new AsyncBitmapLoader(currentContext).
									loadBitmap(weiboRetweetImage, retweetThumbnailUrl, 
								new ImageCallBack() {
									
									public void imageLoad(ImageView imageView, Bitmap bitmap) {
										// TODO Auto-generated method stub
										imageView.setImageBitmap(bitmap);
									}
								});
						if (thumbnialBitmap == null) {
							weiboRetweetImage.setImageResource(IMAGE_RES);
						}else {
							weiboRetweetImage.setImageBitmap(thumbnialBitmap);	
						}
					}else {
						weiboRetweetImage.setVisibility(View.GONE);
					}
					String retweetScreenName = retweetStatus.getUser().getScreenName();
					String retweetText = retweetStatus.getText();
					SpannableString sp = new SpannableString(retweetScreenName + ":" + retweetText);
					sp.setSpan(new ForegroundColorSpan(currentContext.getResources().getColor(
							R.color.cl_timeline_retweet_screen_name)), 
							0, retweetScreenName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					sp.setSpan(new ForegroundColorSpan(currentContext.getResources().getColor(
							R.color.cl_timeline_retweet_text)), 
							retweetScreenName.length(), retweetScreenName.length() + retweetText.length(),
							Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					weiboRetweetText.setText(sp);
					weiboRetweetContent.setVisibility(View.VISIBLE);
				}else {
					weiboRetweetContent.setVisibility(View.GONE);
				}
				
			}else {
				Utility.displayToast(currentContext, R.string.network_error);
			}

			super.onPostExecute(result);
		}
		
	}
	/**
	 * 图片按下监听器
	 * @author yejb
	 *
	 */
	private class OnImageClickListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			//当前微博图片按下
			if (view.getId() == R.id.singleweibo_image) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "OnImageClickListener: image is clicked");
				}
				Intent intent = new Intent(currentContext, WebImageViewer.class);
				intent.putExtra("image_url", imageUrl);
				startActivity(intent);
				//转发的原文图片按下
			}else if (view.getId() == R.id.singleweibo_retweet_image) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "OnImageClickListener: retweet image is clicked");
				}
				Intent intent = new Intent(currentContext, WebImageViewer.class);
				intent.putExtra("image_url", retweetImageUrl);
				startActivity(intent);
			}
		}
		
	}
	
	private class OnBackClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
		
	}
	
	private class OnFavoriteClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			//读取当前微博的收藏状态
			boolean favoritesOperate = toolFavorite.getImageState();
			if (favoritesOperate) {
				favoritesOperate = false;
			}else {
				favoritesOperate = true;
			}
			new favoritesTask(favoritesOperate).execute(weiboId);
		}
		
	}
	
	private class OnCommentClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (toolFavorite.getImageState()) {
				
			}else {
				
			}
		}
		
	}
	
	private class OnTransmitClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class OnShareClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class favoritesTask extends AsyncTask<Long, Void, Favorites> {
		
		//收藏操作 true：添加收藏    false：取消收藏
		private boolean operate;
		
		public favoritesTask(boolean operate) {
			this.operate = operate;
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Favorites doInBackground(Long... params) {
			// TODO Auto-generated method stub
			long id = params[0];
//			sinaweibo4android.api.Status status = null;
			Favorites favorites = null;
			try {
				if (operate) {
					favorites = mWeibo.createFavorite(currentContext, id);
				}else {
					favorites = mWeibo.destroyFavorite(currentContext, id);
				}
				return favorites;
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Favorites result) {
			// TODO Auto-generated method stub
			//操作成功
			if (result != null) {
			//更新收藏操作后的收藏图标状态	
				toolFavorite.setImageState(operate);
				if (operate) {
					Utility.displayToast(currentContext, R.string.create_favorites_success);
				}else {
					Utility.displayToast(currentContext, R.string.destory_favorites_success);
				}
			}else {
				if (operate) {
					Utility.displayToast(currentContext, R.string.create_favorites_failure);
				}else {
					Utility.displayToast(currentContext, R.string.destory_favorites_failure);
				}
			}
			super.onPostExecute(result);
		}
		
	}

}
