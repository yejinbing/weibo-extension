package com.googlecode.WeiboExtension.db;

import sinaweibo4android.api.User;

import com.googlecode.WeiboExtension.AccessInfo;
import com.googlecode.WeiboExtension.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author yejinbing
 *
 */
public class AccountDBAdapter {
	
	public static final String		TAG					= "DBAdatper";

	public static final String		DB_NAME				= "weibo.db";
	public static final int			DB_VERSION			= 1;
	public static final String		TABLE_USER_TOKEN	= "user_token";
	public static final String		KEY_WEIBO_MARK		= "weibo_mark";
	public static final String		KEY_TOKEN			= "token";
	public static final String		KEY_CONSUMER_KEY	= "consumer_key";
	public static final String		KEY_SCREEN_NAME		= "screen_name";
	public static final String		TABLE_USER_INFO		= "user_info";
	public static final String		KEY_USER_ID			= "user_id";
	public static final String		KEY_USER_NAME		= "user_name";
	public static final String		KEY_USER_LOCATION	= "user_location";
	public static final String		KEY_USER_DESCRIPTION = "user_description";
	public static final String		KEY_USER_URL		= "user_url";
	
	public static final int			WEIBO_MARK_SINA		= 0;
	public static final int			WEIBO_MARK_TENCENT	= 1;
	public static final int			WEIBO_MARK_SOHU		= 2;
	
	private static final String		CREATE_TABLE_USER_TOKEN = "CREATE TABLE "
						+ TABLE_USER_TOKEN + " ("
						+ KEY_USER_ID + " long primary key,"
						+ KEY_WEIBO_MARK + " int,"
						+ KEY_TOKEN + " text,"
						+ KEY_CONSUMER_KEY + " text,"
						+ KEY_SCREEN_NAME + " text)";
	private static final String CREATE_TABLE_USER_INFO = "CREATE TABLE "
						+ TABLE_USER_INFO + " ("
						+ KEY_USER_ID + " long primary key,"
						+ KEY_USER_NAME + " text,"
						+ KEY_SCREEN_NAME + " text,"
						+ KEY_USER_LOCATION + " text,"
						+ KEY_USER_DESCRIPTION + " text,"
						+ KEY_USER_URL + " text)";


	public static boolean			hasModify			= false;

	private Context					context;
	private MyDBHelper				dbHelper;
	private SQLiteDatabase			db					= null;
	private static AccountDBAdapter dbAdapter			= null;

	public static synchronized AccountDBAdapter getInstance(Context context) {
		if (dbAdapter == null)
			dbAdapter = new AccountDBAdapter(context);
		return dbAdapter;
	}

	private AccountDBAdapter(Context context) {
		this.context = context;
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
			db.execSQL(CREATE_TABLE_USER_TOKEN);
			db.execSQL(CREATE_TABLE_USER_INFO);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_TOKEN);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_INFO);
			this.onCreate(db);
		}

	}
	/**
	 * 删除一条userToken信息
	 * @param userId 用户id
	 * @return
	 */
	public boolean deleteUserToken(long userId) {
		db = dbHelper.getWritableDatabase();
		boolean b = db.delete(TABLE_USER_TOKEN, KEY_USER_ID + "=" + userId, null) > 0;
		db.close();
		return b;
	}
	
	public Cursor queryUserToken() {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_USER_TOKEN, 
					new String[]{KEY_USER_ID, KEY_WEIBO_MARK, KEY_TOKEN, KEY_CONSUMER_KEY, KEY_SCREEN_NAME}, 
					null, null, null, null, null);
		return cursor;
	}
	
	/**
	 *  获取用户的Token信息
	 * @param userId 用户id
	 * @return 包含用户信息的Cursor
	 */
	public Cursor queryUserToken(long userId) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_USER_TOKEN, 
					new String[]{KEY_USER_ID, KEY_WEIBO_MARK, KEY_TOKEN, KEY_CONSUMER_KEY, KEY_SCREEN_NAME}, 
					KEY_USER_ID + "=" + userId, null, null, null, null);
		return cursor;
	}
	
	public Cursor queryUserToken(String screenName) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_USER_TOKEN, 
					new String[]{KEY_USER_ID, KEY_WEIBO_MARK, KEY_TOKEN, KEY_CONSUMER_KEY, KEY_SCREEN_NAME}, 
					KEY_SCREEN_NAME + "=?", new String[]{screenName}, null, null, null);
		return cursor;
	}
	
	/**
	 * 保存指定的accessInfo
	 * 
	 * @param accessInfo
	 */
	public void saveUserToken(AccessInfo accessInfo) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryUserToken(accessInfo.getUserId());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveUserToken:insert");
				}
				
				ContentValues values = new ContentValues();
				values.put(KEY_USER_ID, accessInfo.getUserId());
				values.put(KEY_WEIBO_MARK, accessInfo.getWeiboMark());
				values.put(KEY_TOKEN, accessInfo.getAccessToken());
				values.put(KEY_CONSUMER_KEY, accessInfo.getConsumerKey());
				values.put(KEY_SCREEN_NAME, accessInfo.getScreenName());
				db.insert(TABLE_USER_TOKEN, null, values);
				db.close();
			}else {
				this.updateUserToken(accessInfo);
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
	public void updateUserToken(AccessInfo accessInfo) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_ID, accessInfo.getUserId());
		values.put(KEY_WEIBO_MARK, accessInfo.getWeiboMark());
		values.put(KEY_TOKEN, accessInfo.getAccessToken());
		values.put(KEY_CONSUMER_KEY, accessInfo.getConsumerKey());
		values.put(KEY_SCREEN_NAME, accessInfo.getScreenName());		
		db.update(TABLE_USER_TOKEN, values, KEY_USER_ID + "=" + accessInfo.getUserId(), null);
		db.close();
	}
	/**
	 * 删除一条userInfo信息
	 * @param userId 用户id
	 * @return
	 */
	public boolean deleteUserInfo(Long userId) {
		db = dbHelper.getWritableDatabase();
		boolean b = db.delete(TABLE_USER_INFO, KEY_USER_ID + "=" + userId, null) > 0;
		db.close();
		return b;
	}
	
	public Cursor queryUserInfo(long userId) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_USER_INFO, 
					new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_SCREEN_NAME, 
				KEY_USER_LOCATION, KEY_USER_DESCRIPTION, KEY_USER_URL}, 
					KEY_USER_ID + "=" + userId, null, null, null, null);
		return cursor;
	}
	/**
	 *  保存指定的UserInfo
	 * @param user
	 */
	public void saveUserInfo(User user) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryUserInfo(user.getId());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveUserInfo:insert");
				}				
				ContentValues values = new ContentValues();
				values.put(KEY_USER_ID, user.getId());
				values.put(KEY_USER_NAME, user.getName());
				values.put(KEY_SCREEN_NAME, user.getScreenName());
				values.put(KEY_USER_LOCATION, user.getLocation());
				values.put(KEY_USER_DESCRIPTION, user.getDescription());
				values.put(KEY_USER_URL, user.getURL() + "");
				db.insert(TABLE_USER_INFO, null, values);
				db.close();
			}else {
				this.updateUserInfo(user);
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
	 * 更新指定的UserInfo
	 * @param user
	 */
	public void updateUserInfo(User user) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USER_ID, user.getId());
		values.put(KEY_USER_NAME, user.getName());
		values.put(KEY_SCREEN_NAME, user.getScreenName());
		values.put(KEY_USER_LOCATION, user.getLocation());
		values.put(KEY_USER_DESCRIPTION, user.getDescription());
		values.put(KEY_USER_URL, user.getURL() + "");
		db.update(TABLE_USER_INFO, values, KEY_USER_ID + "=" + user.getId(), null);
		db.close();
	}

}
