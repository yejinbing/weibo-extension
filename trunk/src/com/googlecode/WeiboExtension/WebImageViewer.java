package com.googlecode.WeiboExtension;

import java.io.BufferedInputStream;
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
import com.googlecode.WeiboExtension.net.ImageCache;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebImageViewer extends Activity{
	
	private static final String 	TAG					= "WebImageViewer";
	
	private String 					imageSavePath 		= "/sdcard/WeiboExtensionDownload/image";
	
	private Context 				currentContext 		= WebImageViewer.this;
	
	private ImageShowView 			mImageShowView;
	private LinearLayout 			llLoading;
	private ProgressBar 			mLoadProgress;
	private TextView 				tvLoadingPercent;
	private Button 					btnRetry;
	
	private ImageView 				titlebarSave;
	private ImageView 				titlebarBack;
	
	private DownImageViewTask 		downImageViewTask 	= new DownImageViewTask();

	private int 					retryCount 			= 3;
	
	private String 					imageUrl;
	private Bitmap 					imageBitmap;
	
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
		llLoading = (LinearLayout) findViewById(R.id.image_viewer_llLoading);
		mLoadProgress = (ProgressBar)findViewById(R.id.image_viewer_progress);
		tvLoadingPercent = (TextView) findViewById(R.id.image_viewer_tvLoadingPercent);
		mLoadProgress.setProgressDrawable(getResources()
				.getDrawable(R.drawable.image_view_progress_bar));
		btnRetry = (Button)findViewById(R.id.image_viewer_btnRetry);
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
	
	private void setProgressPercent(int percent) {
		mLoadProgress.setProgress(percent);
		tvLoadingPercent.setText(percent + "%");
	}
	
	private class DownImageViewTask extends AsyncTask<String, Integer, byte[]> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			llLoading.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected byte[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			String imageUrl = params[0];
			int retriedCount;
			//从缓存中查询
			byte[] image = ImageCache.getInstance().loadImage(imageUrl);
			if (image == null) {
				for (retriedCount = 0; retriedCount < retryCount; retriedCount++) {
					int responseCode = -1;
					try {
						URL url = new URL(imageUrl);
						URLConnection con = url.openConnection();
						con.setConnectTimeout(5000);
						con.setReadTimeout(5000);
						con.connect();
						int fileSize = con.getContentLength();
						InputStream raw = con.getInputStream();
						InputStream in = new BufferedInputStream(raw);
						byte[] data = new byte[fileSize];
						int bytesRead = 0;
						int offset = 0;
						int progress = 0;
						while (offset < fileSize) {
							bytesRead = in.read(data, offset, data.length - offset);
							if (bytesRead == -1) {
								break;
							}
							offset += bytesRead;
							progress = (int) (offset * 100.0 / fileSize);
							publishProgress(progress);
						}
						in.close();
//						bitmap = BitmapFactory.decodeByteArray(data, 0, fileSize);
						image = data;
						ImageCache.getInstance().saveImage(imageUrl, data);
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
			}
			
			return image;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			setProgressPercent(values[0]);
		}

		@Override
		protected void onPostExecute(byte[] result) {
			// TODO Auto-generated method stub
			
			llLoading.setVisibility(View.GONE);
			if (result != null) {
				btnRetry.setVisibility(View.GONE);
				imageBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
				mImageShowView.setImageBitmap(imageBitmap);
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
