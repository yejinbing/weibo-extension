package com.googlecode.WeiboExtension.db;

import sinaweibo4android.api.Comment;
import sinaweibo4android.api.Status;
import com.googlecode.WeiboExtension.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.TextUtils;
import android.util.Log;

public class TimeLineDBAdapter {
	
	public static final String		TAG					= "TimeLineDBAdapter";
	
	public static final String		DB_NAME				= "timeline.db";
	public static final int			DB_VERSION			= 1;
	public static final String		TABLE_FRIENDS_TIMELINE = "friends_timeline";
	public static final String		KEY_ID				= "id";
	public static final String		KEY_USER_ID			= "user_id";
	public static final String		KEY_AVATAR_URL		= "avatar_url";
	public static final String		KEY_SCREEN_NAME		= "screen_name";
	public static final String		KEY_CREATE_TIME		= "create_time";
	public static final String		KEY_TEXT			= "text";
	public static final String		KEY_RETWEET_USER_ID = "retweet_user_id";
	public static final String		KEY_RETWEET_SCREEN_NAME = "retweet_screen_name";
	public static final String		KEY_RETWEET_TEXT	= "retweet_text";
	public static final String		KEY_THUMBNAIL_URL	= "thumbnail_url";
	public static final String		KEY_BMIDDLE_URL		= "bmiddle_url";
	public static final String		KEY_ORIGINAL_URL	= "original_url";
	
	public static final String		TABLE_MY_MENTIONS	= "my_mentions";
	
	public static final String		TABLE_COMMENTS_TO_ME = "comments_to_me";
	public static final String		KEY_COMMENT_ID		= "comment_id";
	public static final String		KEY_COMMENT_USER_ID	= "comment_user_id";
	public static final String		KEY_COMMENT_AVATAR_URL = "comment_avatar_url";
	public static final String		KEY_COMMENT_NAME	= "comment_name";
	public static final String		KEY_COMMENT_CREATE	= "comment_create";
	public static final String		KEY_COMMENT_TEXT	= "comment_text";
	public static final String		KEY_COMMENT_REPLY_TEXT = "comment_reply_text";
	

	private static final String		CREATE_TABLE_FRIENDS_TIMELINE = "CREATE TABLE "
		+ TABLE_FRIENDS_TIMELINE + " ("
		+ KEY_ID + " long primary key,"
		+ KEY_USER_ID + " long,"
		+ KEY_AVATAR_URL + " text,"
		+ KEY_SCREEN_NAME + " text,"
		+ KEY_CREATE_TIME + " long,"
		+ KEY_TEXT + " text,"
		+ KEY_RETWEET_USER_ID + " long,"
		+ KEY_RETWEET_SCREEN_NAME + " text,"
		+ KEY_RETWEET_TEXT + " text,"
		+ KEY_THUMBNAIL_URL + " text,"
		+ KEY_BMIDDLE_URL + " text,"
		+ KEY_ORIGINAL_URL + " text)";
	
	private static final String		CREATE_TABLE_MY_MENTIONS = "CREATE TABLE "
		+ TABLE_MY_MENTIONS + " ("
		+ KEY_ID + " long primary key,"
		+ KEY_USER_ID + " long,"
		+ KEY_AVATAR_URL + " text,"
		+ KEY_SCREEN_NAME + " text,"
		+ KEY_CREATE_TIME + " long,"
		+ KEY_TEXT + " text,"
		+ KEY_RETWEET_USER_ID + " long,"
		+ KEY_RETWEET_SCREEN_NAME + " text,"
		+ KEY_RETWEET_TEXT + " text,"
		+ KEY_THUMBNAIL_URL + " text,"
		+ KEY_BMIDDLE_URL + " text,"
		+ KEY_ORIGINAL_URL + " text)";
	
	private static final String		CREATE_TABLE_COMMENTS_TO_ME = "CREATE TABLE "
		+ TABLE_COMMENTS_TO_ME + " ("
		+ KEY_COMMENT_ID + " long primary key,"
		+ KEY_COMMENT_USER_ID + " long,"
		+ KEY_COMMENT_AVATAR_URL + " text,"
		+ KEY_COMMENT_NAME + " text,"
		+ KEY_COMMENT_CREATE + " long,"
		+ KEY_COMMENT_TEXT + " text,"
		+ KEY_COMMENT_REPLY_TEXT + " text)";
	
//	private Context context;
	private MyDBHelper				dbHelper;
	private SQLiteDatabase			db					= null;
	private static TimeLineDBAdapter dbAdapter			= null;

	public static synchronized TimeLineDBAdapter getInstance(Context context) {
		if (dbAdapter == null)
			dbAdapter = new TimeLineDBAdapter(context);
		return dbAdapter;
	}

	private TimeLineDBAdapter(Context context) {
//		this.context = context;
		dbHelper = new MyDBHelper(context, DB_NAME, null,
				DB_VERSION);
//		 dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1);
	}

	private class MyDBHelper extends SQLiteOpenHelper {

		public MyDBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_FRIENDS_TIMELINE);
			db.execSQL(CREATE_TABLE_MY_MENTIONS);
			db.execSQL(CREATE_TABLE_COMMENTS_TO_ME);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS_TIMELINE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_MENTIONS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS_TO_ME);
			this.onCreate(db);
		}

	}
	
	public int cleanup() {
		int count = 0;
		db = dbHelper.getWritableDatabase();
//		db.execSQL("delete from " + TABLE_FRIENDS_TIMELINE);
//		db.execSQL("update sqlite_sequence SET seq = 0 where name = " + TABLE_FRIENDS_TIMELINE);
		count = db.delete(TABLE_FRIENDS_TIMELINE, null, null);
		count += db.delete(TABLE_MY_MENTIONS, null, null);
		count += db.delete(TABLE_COMMENTS_TO_ME, null, null);
		db.close();
		return count;
	}
	
	public boolean deleteStatus(long id) {
		db = dbHelper.getWritableDatabase();
		boolean b = db.delete(TABLE_FRIENDS_TIMELINE, KEY_ID + "=" + id, null) > 0;
		db.close();
		return b;
	}
	
	public Cursor queryStatus() {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_FRIENDS_TIMELINE, 
					new String[]{KEY_ID, KEY_USER_ID, KEY_AVATAR_URL, KEY_SCREEN_NAME,
				KEY_CREATE_TIME, KEY_TEXT, 
				KEY_RETWEET_USER_ID, KEY_RETWEET_SCREEN_NAME, KEY_RETWEET_TEXT,
				KEY_THUMBNAIL_URL, KEY_BMIDDLE_URL, KEY_ORIGINAL_URL}, 
					null, null, null, null, KEY_ID + " desc");
		return cursor;
	}
	
	public Cursor queryStatus(long id) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_FRIENDS_TIMELINE, 
				new String[]{KEY_ID, KEY_USER_ID, KEY_AVATAR_URL, KEY_SCREEN_NAME,
				KEY_CREATE_TIME, KEY_TEXT, 
				KEY_RETWEET_USER_ID, KEY_RETWEET_SCREEN_NAME, KEY_RETWEET_TEXT,
				KEY_THUMBNAIL_URL, KEY_BMIDDLE_URL, KEY_ORIGINAL_URL},
					KEY_ID + "=" + id, null, null, null, null);
		return cursor;
	}
	
	public void insertStatus(Status status) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryStatus(status.getId());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveUserToken:insert");
				}
				
				ContentValues values = new ContentValues();
				values.put(KEY_ID, status.getId());
				values.put(KEY_USER_ID, status.getUser().getId());
				values.put(KEY_AVATAR_URL, status.getUser().getProfileImageURL().toString());
				values.put(KEY_SCREEN_NAME, status.getUser().getScreenName());
				values.put(KEY_CREATE_TIME, status.getCreatedAt().getTime());
				values.put(KEY_TEXT, status.getText());
				Status retweetStatus = status.getRetweeted_status();
				if (retweetStatus != null) {
					values.put(KEY_RETWEET_USER_ID, retweetStatus.getUser().getId());
					values.put(KEY_RETWEET_SCREEN_NAME, retweetStatus.getUser().getScreenName());
					values.put(KEY_RETWEET_TEXT, retweetStatus.getText());
					String thumbnailPicUrl = retweetStatus.getThumbnail_pic();
					if (!TextUtils.isEmpty(thumbnailPicUrl)) {
						values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
						values.put(KEY_BMIDDLE_URL, retweetStatus.getBmiddle_pic());
						values.put(KEY_ORIGINAL_URL, retweetStatus.getOriginal_pic());
					}
				}				
				String thumbnailPicUrl = status.getThumbnail_pic();
				if (!TextUtils.isEmpty(thumbnailPicUrl)) {
					values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
					values.put(KEY_BMIDDLE_URL, status.getBmiddle_pic());
					values.put(KEY_ORIGINAL_URL, status.getOriginal_pic());
				}
				db.insert(TABLE_FRIENDS_TIMELINE, null, values);
				db.close();
			}else {
				this.updateStatus(status);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}
	/**
	 * 更新指定的accessInfo
	 * 
	 * @param accessInfo
	 */
	public void updateStatus(Status status) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, status.getId());
		values.put(KEY_USER_ID, status.getUser().getId());
		values.put(KEY_AVATAR_URL, status.getUser().getProfileImageURL().toString());
		values.put(KEY_SCREEN_NAME, status.getUser().getScreenName());
		values.put(KEY_CREATE_TIME, status.getCreatedAt().getTime());
		values.put(KEY_TEXT, status.getText());
		Status retweetStatus = status.getRetweeted_status();
		if (retweetStatus != null) {
			values.put(KEY_RETWEET_USER_ID, retweetStatus.getUser().getId());
			values.put(KEY_RETWEET_SCREEN_NAME, retweetStatus.getUser().getScreenName());
			values.put(KEY_RETWEET_TEXT, retweetStatus.getText());
			String thumbnailPicUrl = retweetStatus.getThumbnail_pic();
			if (!TextUtils.isEmpty(thumbnailPicUrl)) {
				values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
				values.put(KEY_BMIDDLE_URL, retweetStatus.getBmiddle_pic());
				values.put(KEY_ORIGINAL_URL, retweetStatus.getOriginal_pic());
			}
		}				
		String thumbnailPicUrl = status.getThumbnail_pic();
		if (!TextUtils.isEmpty(thumbnailPicUrl)) {
			values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
			values.put(KEY_BMIDDLE_URL, status.getBmiddle_pic());
			values.put(KEY_ORIGINAL_URL, status.getOriginal_pic());
		}	
		db.update(TABLE_FRIENDS_TIMELINE, values, KEY_ID + "=" + status.getId(), null);
		db.close();
	}
	
	/**
	 * 以下是my mentions的方法
	 */
	public boolean deleteMyMention(long id) {
		db = dbHelper.getWritableDatabase();
		boolean b = db.delete(TABLE_MY_MENTIONS, KEY_ID + "=" + id, null) > 0;
		db.close();
		return b;
	}
	
	public Cursor queryMyMentions() {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_MY_MENTIONS, 
					new String[]{KEY_ID, KEY_USER_ID, KEY_AVATAR_URL, KEY_SCREEN_NAME,
				KEY_CREATE_TIME, KEY_TEXT, 
				KEY_RETWEET_USER_ID, KEY_RETWEET_SCREEN_NAME, KEY_RETWEET_TEXT,
				KEY_THUMBNAIL_URL, KEY_BMIDDLE_URL, KEY_ORIGINAL_URL},
					null, null, null, null, KEY_ID + " desc");
		return cursor;
	}
	
	public Cursor queryMyMentions(long id) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_MY_MENTIONS, 
				new String[]{KEY_ID, KEY_USER_ID, KEY_AVATAR_URL, KEY_SCREEN_NAME,
				KEY_CREATE_TIME, KEY_TEXT, 
				KEY_RETWEET_USER_ID, KEY_RETWEET_SCREEN_NAME, KEY_RETWEET_TEXT,
				KEY_THUMBNAIL_URL, KEY_BMIDDLE_URL, KEY_ORIGINAL_URL},
					KEY_ID + "=" + id, null, null, null, null);
		return cursor;
	}
	
	public void insertMyMention(Status status) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryStatus(status.getId());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveUserToken:insert");
				}
				
				ContentValues values = new ContentValues();
				values.put(KEY_ID, status.getId());
				values.put(KEY_USER_ID, status.getUser().getId());
				values.put(KEY_AVATAR_URL, status.getUser().getProfileImageURL().toString());
				values.put(KEY_SCREEN_NAME, status.getUser().getScreenName());
				values.put(KEY_CREATE_TIME, status.getCreatedAt().getTime());
				values.put(KEY_TEXT, status.getText());
				Status retweetStatus = status.getRetweeted_status();
				if (retweetStatus != null) {
					values.put(KEY_RETWEET_USER_ID, retweetStatus.getUser().getId());
					values.put(KEY_RETWEET_SCREEN_NAME, retweetStatus.getUser().getScreenName());
					values.put(KEY_RETWEET_TEXT, retweetStatus.getText());
					String thumbnailPicUrl = retweetStatus.getThumbnail_pic();
					if (!TextUtils.isEmpty(thumbnailPicUrl)) {
						values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
						values.put(KEY_BMIDDLE_URL, retweetStatus.getBmiddle_pic());
						values.put(KEY_ORIGINAL_URL, retweetStatus.getOriginal_pic());
					}
				}				
				String thumbnailPicUrl = status.getThumbnail_pic();
				if (!TextUtils.isEmpty(thumbnailPicUrl)) {
					values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
					values.put(KEY_BMIDDLE_URL, status.getBmiddle_pic());
					values.put(KEY_ORIGINAL_URL, status.getOriginal_pic());
				}
				db.insert(TABLE_MY_MENTIONS, null, values);
				db.close();
			}else {
				this.updateStatus(status);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}
	/**
	 * 
	 */
	public void updateMyMention(Status status) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, status.getId());
		values.put(KEY_USER_ID, status.getUser().getId());
		values.put(KEY_AVATAR_URL, status.getUser().getProfileImageURL().toString());
		values.put(KEY_SCREEN_NAME, status.getUser().getScreenName());
		values.put(KEY_CREATE_TIME, status.getCreatedAt().getTime());
		values.put(KEY_TEXT, status.getText());
		Status retweetStatus = status.getRetweeted_status();
		if (retweetStatus != null) {
			values.put(KEY_RETWEET_USER_ID, retweetStatus.getUser().getId());
			values.put(KEY_RETWEET_SCREEN_NAME, retweetStatus.getUser().getScreenName());
			values.put(KEY_RETWEET_TEXT, retweetStatus.getText());
			String thumbnailPicUrl = retweetStatus.getThumbnail_pic();
			if (!TextUtils.isEmpty(thumbnailPicUrl)) {
				values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
				values.put(KEY_BMIDDLE_URL, retweetStatus.getBmiddle_pic());
				values.put(KEY_ORIGINAL_URL, retweetStatus.getOriginal_pic());
			}
		}				
		String thumbnailPicUrl = status.getThumbnail_pic();
		if (!TextUtils.isEmpty(thumbnailPicUrl)) {
			values.put(KEY_THUMBNAIL_URL, thumbnailPicUrl);
			values.put(KEY_BMIDDLE_URL, status.getBmiddle_pic());
			values.put(KEY_ORIGINAL_URL, status.getOriginal_pic());
		}
		db.update(TABLE_MY_MENTIONS, values, KEY_ID + "=" + status.getId(), null);
		db.close();
	}
	
	/**
	 * 以下是comments to me的方法
	 */
	public boolean deleteCommentToMe(long id) {
		db = dbHelper.getWritableDatabase();
		boolean b = db.delete(TABLE_COMMENTS_TO_ME, KEY_COMMENT_ID+ "=" + id, null) > 0;
		db.close();
		return b;
	}
	
	public Cursor queryCommentsToMe() {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_COMMENTS_TO_ME, 
					new String[]{KEY_COMMENT_ID, KEY_COMMENT_USER_ID, KEY_COMMENT_AVATAR_URL, 
				KEY_COMMENT_NAME, KEY_COMMENT_CREATE, KEY_COMMENT_TEXT, KEY_COMMENT_REPLY_TEXT}, 
					null, null, null, null, KEY_COMMENT_ID + " desc");
		return cursor;
	}
	
	public Cursor queryCommentsToMe(long id) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_COMMENTS_TO_ME, 
					new String[]{KEY_COMMENT_ID, KEY_COMMENT_USER_ID, KEY_COMMENT_AVATAR_URL, 
				KEY_COMMENT_NAME, KEY_COMMENT_CREATE, KEY_COMMENT_TEXT, KEY_COMMENT_REPLY_TEXT}, 
					KEY_COMMENT_ID + "=" + id, null, null, null, null);
		return cursor;
	}
	
	public void insertCommentToMe(Comment comment) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryStatus(comment.getId());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveUserToken:insert");
				}
				
				ContentValues values = new ContentValues();
				values.put(KEY_COMMENT_ID, comment.getId());
				values.put(KEY_COMMENT_USER_ID, comment.getUser().getId());
				values.put(KEY_COMMENT_AVATAR_URL, comment.getUser().getProfileImageURL().toString());
				values.put(KEY_COMMENT_NAME, comment.getUser().getScreenName());
				values.put(KEY_COMMENT_CREATE, comment.getCreatedAt().getTime());
				values.put(KEY_COMMENT_TEXT, comment.getText());
				values.put(KEY_COMMENT_REPLY_TEXT, comment.getreplyComment().getText());

				db.insert(TABLE_COMMENTS_TO_ME, null, values);
				db.close();
			}else {
				this.updateCommentToMe(comment);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}
	
	public void updateCommentToMe(Comment comment) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_COMMENT_ID, comment.getId());
		values.put(KEY_COMMENT_USER_ID, comment.getUser().getId());
		values.put(KEY_COMMENT_AVATAR_URL, comment.getUser().getProfileImageURL().toString());
		values.put(KEY_COMMENT_NAME, comment.getUser().getScreenName());
		values.put(KEY_COMMENT_CREATE, comment.getCreatedAt().getTime());
		values.put(KEY_COMMENT_TEXT, comment.getText());
		values.put(KEY_COMMENT_REPLY_TEXT, comment.getreplyComment().getText());
		
		db.update(TABLE_COMMENTS_TO_ME, values, KEY_COMMENT_ID + "=" + comment.getId(), null);
		db.close();
	}
}
