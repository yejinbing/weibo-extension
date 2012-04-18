package com.googlecode.WeiboExtension.net;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;

/**
 * 全局图片缓存
 * @author yejb
 *
 */
public class ImageCache {

	private static HashMap<String, SoftReference<byte[]>> imageCache = null;
	
	private static ImageCache		mImageCache			= null;
	
	public synchronized static ImageCache getInstance() {
		if (mImageCache == null) {
			mImageCache = new ImageCache();
			imageCache = new HashMap<String, SoftReference<byte[]>>();
		}
		return mImageCache;
	}
	
	public byte[] loadImage(String imageUrl) {
		
		if(imageCache.containsKey(imageUrl))
		{
			SoftReference<byte[]> reference = imageCache.get(imageUrl);
			byte[] image = reference.get();
			if(image != null) {
				return image;
			}
		}
		
		return null;
	}
	
	public void saveImage(String imageUrl, byte[] image) {
		imageCache.put(imageUrl, new SoftReference<byte[]>(image));
	}
}
