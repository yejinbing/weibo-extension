package com.googlecode.WeiboExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.View.ImageShowView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class WebImageViewer extends Activity{
	
	private static final String TAG = "WebImageViewer";
	
	private String imageSavePath = "/sdcard/WeiboExtensionDownload/image";
	
	private Context currentContext = WebImageViewer.this;
	
	private ImageShowView mImageShowView;
	private ProgressBar mLoadProgress;
	private Button btnRetry;
	
	private ImageView titlebarSave;
	private ImageView titlebarBack;
	
	private DownImageViewTask downImageViewTask = new DownImageViewTask();

	private int retryCount = 3;
	
	private String imageUrl;
	private Bitmap imageBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.image_viewer);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.image_viewer_titlebar);		
		
		titlebarSave = (ImageView)findViewById(R.id.image_viewer_titlebar_save);
		titlebarBack = (ImageView)findViewById(R.id.image_viewer_titlebar_back);
		
		titlebarSave.setOnClickListener(new OnTitlebarClickListener());
		titlebarBack.setOnClickListener(new OnTitlebarClickListener());
		
		mImageShowView = (ImageShowView)findViewById(R.id.image_viewer_image_show);
		mLoadProgress = (ProgressBar)findViewById(R.id.image_viewer_progress);
		btnRetry = (Button)findViewById(R.id.image_viewer_retry);
		btnRetry.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View view) {
				// TODO Auto-generated method stub
				if (imageUrl != null) {
					btnRetry.setVisibility(View.GONE);
					downImageViewTask.execute(imageUrl);
				}
			}
		});
		
		if (savedInstanceState != null) {

		}else {
			Intent intent = getIntent();
			imageUrl = intent.getStringExtra("image_url");			
			if (imageUrl != null) {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "intent: imageUrl:" + imageUrl);
				}
				downImageViewTask.execute(imageUrl);
			}else {
				if (Constants.Config.DEBUG) {
					Log.d(TAG, "intent: imageUrl is null");
				}
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}	


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (downImageViewTask != null && downImageViewTask.getStatus() != AsyncTask.Status.FINISHED) {
					downImageViewTask.cancel(true);
				}
				finish();
				break;
		}
		return false;
	}
	
	private class DownImageViewTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mLoadProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			int retriedCount;
			Bitmap bitmap = null;
			for (retriedCount = 0; retriedCount < retryCount; retriedCount++) {
				int responseCode = -1;
				try {
					URL url = new URL(params[0]);
					URLConnection con = url.openConnection();
					con.setConnectTimeout(5000);
					con.setReadTimeout(5000);
					con.connect();
					InputStream bitmapIs = con.getInputStream();
					bitmap = BitmapFactory.decodeStream(bitmapIs);
					break;
				}catch (MalformedURLException e) {
					e.printStackTrace();
				}catch (UnsupportedEncodingException e) {
					// TODO: handle exception
					e.printStackTrace();
				}catch (SocketTimeoutException e) {
					Log.e(TAG, "downImageView:timeout");
					responseCode++;
				}catch (IOException e) {
					e.printStackTrace();
				}

			}		
			
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			
			mLoadProgress.setVisibility(View.GONE);
			if (result != null) {
				btnRetry.setVisibility(View.GONE);
				imageBitmap = result;
				mImageShowView.setImageBitmap(result);
			}else {
				Utility.displayToast(currentContext, R.string.network_error);
				btnRetry.setVisibility(View.VISIBLE);
			}
			
			super.onPostExecute(result);
		}	
		
	}
	
	private class OnTitlebarClickListener implements OnClickListener {

		public void onClick(View view) {
			// TODO Auto-generated method stub
			if (view.getId() == R.id.image_viewer_titlebar_save) {
				Drawable drawable = mImageShowView.getDrawable();
				if (drawable != null) {
					String imagePath = saveImage2Sdcard(imageSavePath, 
							imageBitmap, Utility.convertUrlToFilename(imageUrl));
					if (imagePath != null) {
						Utility.displayToast(currentContext, currentContext.getResources().getString(R.string.image_save_to)
								+ imageSavePath);
					}else {
						Utility.displayToast(currentContext, R.string.image_save_failure);
					}
				}else {
					Utility.displayToast(currentContext, R.string.no_image);
				}
			}else if (view.getId() == R.id.image_viewer_titlebar_back) {
				finish();
			}
		}
		
	}
	/**
	 * 将Bitmap以jpg格式保存到指定文件夹中
	 * @param path 保存的文件夹
	 * @param bitmap 要保存的Bitmap
	 * @param name 保存的名称
	 * @return 保存后的图片绝对路径
	 */
	private String saveImage2Sdcard(String path, Bitmap bitmap, String name) {
		String imagePath = null;
		File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		File bitmapFile = new File(path + "/" + name + ".jpg");
		if (!bitmapFile.exists()) {
			try {
				bitmapFile.createNewFile();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream fos;
		try{
			fos = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 
					100, fos);
			fos.close();
			imagePath = bitmapFile.getAbsolutePath();
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return imagePath;
	}

}
