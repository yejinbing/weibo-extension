package com.googlecode.WeiboExtension.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import com.googlecode.WeiboExtension.Utility.Utility;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * @author oneRain
 **/
public class AsyncBitmapLoader
{
	private String ThumbnailDir;
	/**
	 * 内存图片软引用缓冲
	 */
	private HashMap<String, SoftReference<Bitmap>> imageCache = null;
	
	public AsyncBitmapLoader(Context context) {
		ThumbnailDir =  context.getExternalCacheDir() + "/Thumbnail";
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}
	
	public Bitmap loadBitmap(final ImageView imageView, final String imageURL, final ImageCallBack imageCallBack) {
		//在内存缓存中，则返回Bitmap对象
		if(imageCache.containsKey(imageURL))
		{
			SoftReference<Bitmap> reference = imageCache.get(imageURL);
			Bitmap bitmap = reference.get();
			if(bitmap != null) {
				return bitmap;
			}
		}else {
			/**
			 * 加上一个对本地缓存的查找
			 */
//			String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1);
			String bitmapName = Utility.convertUrlToFilename(imageURL);
			File cacheDir = new File(ThumbnailDir);
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			File[] cacheFiles = cacheDir.listFiles();
			int i = 0;
			for(; i < cacheFiles.length; i++) {
				if(bitmapName.equals(cacheFiles[i].getName())) {
					break;
				}
			}
			
			if(i < cacheFiles.length) {
				return BitmapFactory.decodeFile(ThumbnailDir + "/" + bitmapName);
			}
		}
		
		final Handler handler = new Handler() {
			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg ){
				// TODO Auto-generated method stub
				imageCallBack.imageLoad(imageView, (Bitmap)msg.obj);
			}
		};
		
		//如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片
		new Thread()
		{
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				URL url;
				try {
					url = new URL(imageURL);
					URLConnection con = url.openConnection();
					con.connect();
					InputStream bitmapIs = con.getInputStream();
					
					Bitmap bitmap = BitmapFactory.decodeStream(bitmapIs);
					imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
					
					File dir = new File(ThumbnailDir);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					
//					File bitmapFile = new File(ThumbnailDir + "/" + imageURL.substring(imageURL.lastIndexOf("/") + 1));
					File bitmapFile = new File(ThumbnailDir + "/" + Utility.convertUrlToFilename(imageURL));
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
					}catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		}.start();
		
		return null;
	}
	/**
	 * 回调接口
	 * @author onerain
	 *
	 */
	public interface ImageCallBack
	{
		public void imageLoad(ImageView imageView, Bitmap bitmap);
	}
}