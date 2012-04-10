package com.googlecode.WeiboExtension.db;

import sinaweibo4android.api.Emotion;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import com.googlecode.WeiboExtension.Constants;

public class EmotionsDBAdapter {
	public static final String TAG = "EmotionsDBAdapter";

	public static final String DB_NAME = "emotions.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE_EMOTIONS = "emotions";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_TYPE = "type";
	public static final String KEY_URL = "url";
	public static final String KEY_VALUE = "value";
	public static final String KEY_DATA = "data";
	
	private static final String CREATE_TABLE_EMOTIONS = "CREATE TABLE "
						+ TABLE_EMOTIONS + " ("
						+ KEY_CATEGORY + " text,"
						+ KEY_TYPE + " text,"
						+ KEY_URL + " text,"
						+ KEY_VALUE + " text,"
						+ KEY_DATA + " data)";



	public static boolean hasModify = false;

	private Context context;
	private MyDBHelper dbHelper;
	private SQLiteDatabase db = null;
	private static EmotionsDBAdapter dbAdapter = null;

	public static synchronized EmotionsDBAdapter getInstance(Context context) {
		if (dbAdapter == null)
			dbAdapter = new EmotionsDBAdapter(context);
		return dbAdapter;
	}

	private EmotionsDBAdapter(Context context) {
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
			db.execSQL(CREATE_TABLE_EMOTIONS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMOTIONS);
			this.onCreate(db);
		}

	}
	/**
	 * 查询所有的普通表情
	 * @return
	 */
	public Cursor queryEmotion() {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_EMOTIONS, 
					new String[]{KEY_CATEGORY, KEY_TYPE, KEY_URL, KEY_VALUE, KEY_DATA}, 
					null, null, null, null, null);
		return cursor;
	}
	/**
	 * 根据一条表情的值来查询一个普通表情的内容
	 * @param value
	 * @return
	 */
	public Cursor queryEmotion(String value) {
		db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_EMOTIONS, 
				new String[]{KEY_CATEGORY, KEY_TYPE, KEY_URL, KEY_VALUE, KEY_DATA},  
				KEY_VALUE + "=?", new String[]{value}, null, null, null);
		return cursor;
	}
	public void refEmotion(String value, byte[] bytes) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_DATA, bytes);
		db.update(TABLE_EMOTIONS, values, KEY_VALUE + "=?", new String[]{value});
		db.close();
	}
	/**
	 * 保存一个表情
	 * @param emotion
	 */
	public void saveEmotion(Emotion emotion) {
		Cursor cur = null;
		try {
			db = dbHelper.getWritableDatabase();
			cur = queryEmotion(emotion.getValue());
			if (cur.getCount() == 0) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "saveEmotion:insert");
				}
				
				ContentValues values = new ContentValues();
				values.put(KEY_CATEGORY, emotion.getCategory());
				values.put(KEY_TYPE, emotion.getType());
				values.put(KEY_URL, emotion.getUrl());
				values.put(KEY_VALUE, emotion.getValue());
				db.insert(TABLE_EMOTIONS, null, values);
				db.close();
			}else {
				this.updateEmotion(emotion);
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
	 * 更新指定的表情
	 * @param emotion
	 */
	public void updateEmotion(Emotion emotion) {
		if (db != null || db.isOpen())
			db.close();
		db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_CATEGORY, emotion.getCategory());
		values.put(KEY_TYPE, emotion.getType());
		values.put(KEY_URL, emotion.getUrl());
		values.put(KEY_VALUE, emotion.getValue());
		db.update(TABLE_EMOTIONS, values, KEY_VALUE + "=?", new String[]{emotion.getValue()});
		db.close();
	}
}
