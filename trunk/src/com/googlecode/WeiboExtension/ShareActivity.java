/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.WeiboExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.googlecode.WeiboExtension.EventStream.Settings;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.View.ToolBarPanelView;
import com.googlecode.WeiboExtension.View.ToolBarView;
import com.googlecode.WeiboExtension.db.EmotionsDBAdapter;

import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Emotion;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * A dialog activity for sharing any text or image message to weibo. Three
 * parameters , accessToken, tokenSecret, consumer_key, are needed, otherwise a
 * WeiboException will be throwed.
 * 
 * ShareActivity should implement an interface, RequestListener which will
 * return the request result.
 * 
 * @author ZhangJie (zhangjie2@staff.sina.com.cn)
 */

public class ShareActivity extends Activity implements OnClickListener{

	private static final String		TAG					= "ShareActivity";
	
	private Context					currentContext		= ShareActivity.this;
	
	private static final int		REQUEST_CODE_TAKE_PICTURE = 1;
	
	public static final String		EXTRA_WEIBO_CONTENT = "com.googlecode.WeiboExtension.content";
    public static final String		EXTRA_PIC_URI 		= "com.googlecode.WeiboExtension.pic.uri";
    
    public static final String		SAVED_STATE_ACCESS_TOKEN = "com.googlecode.WeiboExtension.accesstoken";
    public static final String		SAVED_STATE_COSUMER_KEY = "com.googlecode.WeiboExtension.consumerkey";
    public static final String		EXTRA_ACCESS_TOKEN = "com.googlecode.WeiboExtension.accesstoken";
    public static final String		EXTRA_COSUMER_KEY = "com.googlecode.WeiboExtension.consumerkey";
	
    private TextView				mTextNum;
    private Button					mSend;
    private EditText				mEdit;
    private FrameLayout				mPiclayout;
    
    private GridView				mEmotionsView;
    private ArrayList<HashMap<String, Object>> mEmotions;
    private SimpleAdapter			mEmotionsAdapter;
    
    private ToolBarPanelView		mToolBarPanel;
    private ToolBarView				toolBack;
    private ToolBarView				toolEmotion;
    private ToolBarView				toolPhoto;
    private ToolBarView				toolAt;
    private ToolBarView				toolLocation;

    private String					mPicPath = "";
    private String					mContent = "";
    
    private Weibo					mWeibo;

    public static final int			WEIBO_MAX_LENGTH	= 140;
    
    private ShareTask				mShareTask;
    private GetEmotionsTask			mGetEmotionsTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.share_weibo_view);
        
        mEmotionsView = (GridView)findViewById(R.id.share_weibo_view_emotions);
        initEmotions();
        
        mToolBarPanel = (ToolBarPanelView)findViewById(R.id.share_weibo_view_toolbarpanel);      
        initToolBar();

        Intent in = this.getIntent();
        mPicPath = in.getStringExtra(EXTRA_PIC_URI);
        mContent = in.getStringExtra(EXTRA_WEIBO_CONTENT);

        Button close = (Button) this.findViewById(R.id.share_weibo_view_btnClose);
        close.setOnClickListener(this);
        mSend = (Button) this.findViewById(R.id.share_weibo_view_btnSend);
        mSend.setOnClickListener(this);
        LinearLayout total = (LinearLayout) this.findViewById(R.id.ll_text_limit_unit);
        total.setOnClickListener(this);
        mTextNum = (TextView) this.findViewById(R.id.tv_text_limit);
        ImageView picture = (ImageView) this.findViewById(R.id.share_weibo_view_ivDelPic);
        picture.setOnClickListener(this);

        mEdit = (EditText) this.findViewById(R.id.share_weibo_view_etEdit);
        mEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mText = mEdit.getText().toString();
                int len = mText.length();
                if (len <= WEIBO_MAX_LENGTH) {
                    len = WEIBO_MAX_LENGTH - len;
                    mTextNum.setTextColor(R.color.text_num_gray);
                    if (!mSend.isEnabled())
                        mSend.setEnabled(true);
                } else {
                    len = len - WEIBO_MAX_LENGTH;

                    mTextNum.setTextColor(Color.RED);
                    if (mSend.isEnabled())
                        mSend.setEnabled(false);
                }
                mTextNum.setText(String.valueOf(len));
            }
        });
        if (mContent != null) {
          mEdit.setText(mContent);
        }

        mPiclayout = (FrameLayout) ShareActivity.this.findViewById(R.id.share_weibo_view_flPic);
        if (TextUtils.isEmpty(this.mPicPath)) {
            mPiclayout.setVisibility(View.GONE);
        } else {
            mPiclayout.setVisibility(View.VISIBLE);
            File file = new File(mPicPath);
            if (file.exists()) {
                Bitmap pic = BitmapFactory.decodeFile(this.mPicPath);
                ImageView image = (ImageView) this.findViewById(R.id.share_weibo_view_ivImage);
                image.setImageBitmap(pic);
            } else {
                mPiclayout.setVisibility(View.GONE);
            }
        }
    }
    
    @Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
    	Log.d(TAG, "onLowMemory");
		super.onLowMemory();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume");
		super.onResume();
		Settings mSettings = new Settings(currentContext);
		String token = mSettings.getToken();
		String tokenSecret = mSettings.getTokenSecret();	
		AccessToken accessToken = new AccessToken(token, tokenSecret);
		mWeibo = Weibo.getInstance();
		mWeibo.setAccessToken(accessToken);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
			if (resultCode == Activity.RESULT_OK) {
				String sdStatus = Environment.getExternalStorageState();
				if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
					Log.v(TAG, "SD card is not avaiable/writeable right now.");
					return;
				}
				Log.d(TAG, "success");
				
				Bundle bundle = data.getExtras();
				Bitmap bitmap = (Bitmap) bundle.get("data");
				FileOutputStream b = null;
				String fileName = getExternalCacheDir().getPath() + "/share_weibo_capture_pic.jpg";
				
				try {  
	                b = new FileOutputStream(fileName);  
	                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件  
	            } catch (FileNotFoundException e) {  
	                e.printStackTrace();  
	            } finally {  
	                try {  
	                    b.flush();  
	                    b.close();  
	                } catch (IOException e) {  
	                    e.printStackTrace();  
	                }  
	            }
				mPicPath = fileName;
				Log.d(TAG, "mPicPath:" + mPicPath);
				mPiclayout.setVisibility(View.VISIBLE);
				ImageView image = (ImageView) this.findViewById(R.id.share_weibo_view_ivImage);
				image.setImageBitmap(bitmap);
			}
		}
	}
    
    private void initToolBar() {
		toolBack = new ToolBarView(currentContext, R.string.back,
				R.drawable.singleweibo_back_normal, -1);
		mToolBarPanel.addTool(toolBack);
		toolEmotion = new ToolBarView(currentContext, R.string.expression, 
				R.drawable.share_weibo_expression, -1);
		mToolBarPanel.addTool(toolEmotion);
		toolPhoto = new ToolBarView(currentContext, R.string.photo, 
				R.drawable.share_weibo_photo, -1);
		mToolBarPanel.addTool(toolPhoto);
		toolAt = new ToolBarView(currentContext, R.string.at,
				R.drawable.share_weibo_at, -1);
		mToolBarPanel.addTool(toolAt);
		toolLocation = new ToolBarView(currentContext, R.string.location, 
				R.drawable.share_weibo_location, -1);
		mToolBarPanel.addTool(toolLocation);
		
		toolBack.setOnClickListener(new OnBackClickListener());
		toolPhoto.setOnClickListener(new OnPhotoClickListener());
		toolEmotion.setOnClickListener(new OnEmotionClickListener());
	}
    
    private void initEmotions() {
        mEmotions = new ArrayList<HashMap<String,Object>>();
        Cursor cursor = null;
        EmotionsDBAdapter dbAdapter = EmotionsDBAdapter.getInstance(currentContext);
    	try {
    		cursor = dbAdapter.queryEmotion();
    		while (cursor.moveToNext()) {
    			String value = cursor.getString(cursor.getColumnIndex(EmotionsDBAdapter.KEY_VALUE));
    			byte[] bytes = cursor.getBlob(cursor.getColumnIndex(EmotionsDBAdapter.KEY_DATA));
    			
    			HashMap<String, Object> map = new HashMap<String, Object>();
    			map.put("value", value);
    			if (bytes != null)  {
    				Bitmap emotion = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    				map.put("emotion", emotion);
    			}else {
    				map.put("emotion", R.drawable.download_gif3);
    			}
    	        
    	        mEmotions.add(map);
    		}
    	}catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
    	}finally {
    		if (cursor != null) {
    			cursor.close();
    		}
    	} 
        mEmotionsAdapter = new SimpleAdapter(currentContext, 
        		mEmotions,
        		R.layout.emotions_gridview_item, 
        		new String[]{"emotion"},
        		new int[]{R.id.emotions_gridview_item_emotion});
        mEmotionsView.setAdapter(mEmotionsAdapter);
        mEmotionsAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				// TODO Auto-generated method stub
				//判断imageview的资源类型，进行匹配
				if (view instanceof ImageView) {
					ImageView iv = (ImageView)view;
					if (data instanceof Integer) {
						iv.setImageResource((Integer)data);
						return true;
					}else if (data instanceof Drawable) {
						iv.setImageDrawable((Drawable)data);
						return true;
					}else if (data instanceof Bitmap) {
						iv.setImageBitmap((Bitmap)data);
						return true;
					}					
					return false;
				}else {
					return false;
				}
			}
		});
        mEmotionsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				//将表情符号替换表情值
				String value = (String) mEmotions.get(position).get("value");
				Object data = (Bitmap) mEmotions.get(position).get("emotion");
				ImageSpan imageSpan;
				if (data instanceof Bitmap) {
					imageSpan = new ImageSpan((Bitmap) data);
					SpannableString spannableString = new SpannableString(value);
					spannableString.setSpan(imageSpan, 0, value.length(), 
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					mEdit.append(spannableString);
				}			
			}
		});
    }

    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.share_weibo_view_btnClose) {
            finish();
        } else if (viewId == R.id.share_weibo_view_btnSend) {
        	if (mWeibo.getAccessToken() != null && !TextUtils.isEmpty((String) (mWeibo.getAccessToken().getToken()))) {
        		if (mShareTask != null && mShareTask.getStatus() != AsyncTask.Status.FINISHED) {
            		mShareTask.cancel(true);
        		}
            	mShareTask = new ShareTask();
            	mShareTask.execute(mEdit.getText().toString(), mPicPath);
            } else {
            	Utility.displayToast(currentContext, R.string.please_login);
            }
        	
        } else if (viewId == R.id.ll_text_limit_unit) {
            Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
                    .setMessage(R.string.delete_all)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mEdit.setText("");
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        } else if (viewId == R.id.share_weibo_view_ivDelPic) {
            Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
                    .setMessage(R.string.del_pic)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	mPicPath = null;	//删除图片路径
                            mPiclayout.setVisibility(View.GONE);
                        }
                    }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        }
    }
    
    private class OnBackClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
		
	}
    
    private class OnEmotionClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mEmotionsView.getVisibility() == View.GONE) {
				mEmotionsView.setVisibility(View.VISIBLE);
				
				if (mGetEmotionsTask != null && mGetEmotionsTask.getStatus() != AsyncTask.Status.FINISHED) {
					mGetEmotionsTask.cancel(true);
        		}
				mGetEmotionsTask = new GetEmotionsTask();
				mGetEmotionsTask.execute();
			}else if (mEmotionsView.getVisibility() == View.VISIBLE) {
				mEmotionsView.setVisibility(View.GONE);
			}
		}
    	
    }
    
    private class OnPhotoClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		}
		
	}
    /**
     * 发送一条微博的异步任务
     * @author yejb
     *
     */
    private class ShareTask extends AsyncTask<String, Void, sinaweibo4android.api.Status> {


		@Override
		protected sinaweibo4android.api.Status doInBackground(String... params) {
			// TODO Auto-generated method stub
			String text = params[0];
			String picPath = params[1];
			sinaweibo4android.api.Status status = null;
			try {
				if (!TextUtils.isEmpty(picPath)) {
                	status = mWeibo.upload(currentContext, text, picPath);
                } else {
                    // Just update a text weibo!
                	status = mWeibo.update(currentContext, text);
                }
			}catch (WeiboException e) {
				// TODO: handle exception
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
//				if (e.getStatusCode() == 90000) {
//					return new ArrayList<Map<String,Object>>(0);
//				}else {
					return null;
//				}
			}
			
			return status;
		}
		
		@Override
		protected void onPostExecute(sinaweibo4android.api.Status result) {
			// TODO Auto-generated method stub
			if (result != null) {
				Utility.displayToast(currentContext, R.string.send_success);
			}else {
				Utility.displayToast(currentContext, R.string.send_failed);
			}
			super.onPostExecute(result);
		}
    	
    }
    /**
     * 从新浪官方下载表情详情的异步任务
     * @author yejb
     *
     */
    private class GetEmotionsTask extends AsyncTask<View, Void, List<Emotion>> {

		@Override
		protected List<Emotion> doInBackground(View... arg0) {
			// TODO Auto-generated method stub
			List<Emotion> emotions = null;
			try {
				emotions = mWeibo.getEmotions(currentContext, "face");
				return emotions;
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
				if (e.getStatusCode() == 90000) {
					return new ArrayList<Emotion>(0);
				}else {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(List<Emotion> result) {
			// TODO Auto-generated method stub
			if (result != null) {
				EmotionsDBAdapter dbAdapter = EmotionsDBAdapter.getInstance(currentContext);
				for (Emotion emotion : result) {
					if (emotion.isCommon()) {
						dbAdapter.saveEmotion(emotion);
						System.out.println("emotion,value:" + emotion.getValue());
					}
				}
				refEmotion();
			}else {
				
			}
			super.onPostExecute(result);
		}
    	
    }
    /**
     * 刷新表情
     */
    private void refEmotion() {
    	Cursor cursor = null;
    	EmotionsDBAdapter dbAdapter = EmotionsDBAdapter.getInstance(currentContext);
    	try {
    		cursor = dbAdapter.queryEmotion();
    		while (cursor.moveToNext()) {
    			String value = cursor.getString(cursor.getColumnIndex(EmotionsDBAdapter.KEY_VALUE));
    			String emotionUrl = cursor.getString(cursor.getColumnIndex(EmotionsDBAdapter.KEY_URL));
    			new RefEmotionTask(value, emotionUrl).execute();
    		}
    	}catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
    	}finally {
    		if (cursor != null) {
    			cursor.close();
    		}
    	}
    }
    /**
     * 从网络下载表情的异步任务，完成后更新表情gridview和数据库中标清图标数据
     * @author yejb
     *
     */
    private class RefEmotionTask extends AsyncTask<Void, Void, Bitmap> {
    	
    	private String value;
    	private String emotionUrl;
    	
    	public RefEmotionTask(String value, String emotionUrl) {
    		this.value = value;
    		this.emotionUrl = emotionUrl;
    	}

		@Override
		protected Bitmap doInBackground(Void... params) {		
			// TODO Auto-generated method stub
			Bitmap emotion = null;
			try {
				URL url = new URL(emotionUrl);
				URLConnection con = url.openConnection();
				con.connect();
				InputStream bitmapIs = con.getInputStream();		
				emotion = BitmapFactory.decodeStream(bitmapIs);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			return emotion;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			if (result != null) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("value", value);
				map.put("emotion", result);
		        mEmotions.add(map);
		        mEmotionsAdapter.notifyDataSetChanged();
		        
		        Bitmap emotion = result;
		        final ByteArrayOutputStream os = new ByteArrayOutputStream(); 
				emotion.compress(Bitmap.CompressFormat.PNG, 100, os);
				EmotionsDBAdapter dbAdapter = EmotionsDBAdapter.getInstance(currentContext);
				dbAdapter.refEmotion(value, os.toByteArray());
			}else {
				
			}		
			
			super.onPostExecute(result);
		}
    	
    }

}
