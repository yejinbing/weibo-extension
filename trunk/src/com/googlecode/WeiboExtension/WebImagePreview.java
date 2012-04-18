package com.googlecode.WeiboExtension;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.View.GifView;
import com.googlecode.WeiboExtension.net.ImageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

public class WebImagePreview extends PopupWindow{
	
	private static final String 	TAG					= "WebImagePreview";

	protected final View          	anchor;
    protected final PopupWindow   	window;
    
    private final Context         	context;
    
    private Drawable              	background          = null;
    
    private String 					imageUrl;
	
	private View 					imagePopwindow;
	private GifView					gvGif;
	private ProgressBar 			mLoadProgress;
	
	private DownImageViewTask 		downImageViewTask 	= new DownImageViewTask();

	private int 					retryCount 			= 3;
    
	public WebImagePreview(View anchor, String imageUrl) {
        super(anchor);

        this.anchor = anchor;
        this.window = new PopupWindow(anchor.getContext());
        
        this.context = anchor.getContext();
        this.imageUrl = imageUrl;
        
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
        		Context.LAYOUT_INFLATER_SERVICE);
        imagePopwindow = inflater.inflate(R.layout.image_preview, null);
        gvGif = (GifView) imagePopwindow.findViewById(R.id.image_preview_gvGif);
        mLoadProgress = (ProgressBar) imagePopwindow.findViewById(R.id.image_preview_progress);
        background = context.getResources().getDrawable(R.drawable.pic_bg);
	}
	
	protected void preShow() {
        if (imagePopwindow == null) {
            throw new IllegalStateException("需要为弹窗设置布局");
        }
        if (background == null) {
            window.setBackgroundDrawable(new BitmapDrawable());
        } else {
            window.setBackgroundDrawable(background);
        }
        mLoadProgress.setProgressDrawable(context.getResources()
        		.getDrawable(R.drawable.image_preview_progress_bar));
        
        window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setTouchable(true);
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.setContentView(imagePopwindow);
    }
	
	public void show() {
		preShow();
		
		imagePopwindow.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		imagePopwindow.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		window.showAtLocation(this.anchor, Gravity.CENTER, 0, 0);
		
		if (imageUrl != null) {
			downImageViewTask.execute(imageUrl);
		}else {
			Log.d(TAG, "intent: imageUrl is null");
		}
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		Log.d(TAG, "dismiss");
	}

	private void setProgressPercent(int percent) {
		mLoadProgress.setProgress(percent);
	}
	
	private class DownImageViewTask extends AsyncTask<String, Integer, byte[]> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mLoadProgress.setVisibility(View.VISIBLE);
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
						URL url = new URL(params[0]);
						URLConnection con = url.openConnection();
						con.setConnectTimeout(5000);
						con.setReadTimeout(5000);
						con.connect();
						String type = con.getContentType();
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

						ImageCache.getInstance().saveImage(imageUrl, data);
						image = data;
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
			mLoadProgress.setVisibility(View.GONE);
			if (result != null) {
				gvGif.setGif(result);
				gvGif.play();
			}else {
				Utility.displayToast(context, R.string.network_error);
			}
			
			super.onPostExecute(result);
		}	
		
	}
    
}
