/**  
 * MyImageView.java
 * @version 1.0
 * @author Haven
 * @createTime 2011-12-9 下午03:12:30
 * 此类代码是根据android系统自带的ImageViewTouchBase代码修改
 */
package com.googlecode.WeiboExtension.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageShowView extends ImageView {
	
	private static final String TAG = "ImageViewTouchBase";

	// This is the base transformation which is used to show the image
	// initially. The current computation for this shows the image in
	// it's entirety, letterboxing as needed. One could choose to
	// show the image as cropped instead.
	//
	// This matrix is recomputed when we go from the thumbnail image to
	// the full size image.
	protected Matrix mBaseMatrix = new Matrix();

	// This is the supplementary transformation which reflects what
	// the user has done in terms of zooming and panning.
	//
	// This matrix remains the same when we go from the thumbnail image
	// to the full size image.
	protected Matrix mSuppMatrix = new Matrix();

	// This is the final matrix which is computed as the concatentation
	// of the base matrix and the supplementary matrix.
	private final Matrix mDisplayMatrix = new Matrix();

	// Temporary buffer used for getting the values out of a matrix.
	private final float[] mMatrixValues = new float[9];

	// The current bitmap being displayed.
	// protected final RotateBitmap mBitmapDisplayed = new RotateBitmap(null);
	protected Bitmap image = null;

	int mThisWidth = -1, mThisHeight = -1;

	float mMaxZoom = 2.0f;// 最大缩放比例
	float mMinZoom ;// 最小缩放比例

	private int imageWidth;// 图片的原始宽度
	private int imageHeight;// 图片的原始高度
	
	private int screenWidth;		//屏幕宽度
	private int screenHeight;		//屏幕高度

	private float scaleRate;// 图片适应屏幕的缩放比例
	
	private GestureDetector gestureScanner; 
	
	private Context mContext;
	
	public ImageShowView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ImageShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public ImageShowView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		screenWidth = w;
		screenHeight = h;
//		startView();
		arithScaleRate();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	

	/**
	 * 计算图片要适应屏幕需要缩放的比例
	 */
	private void arithScaleRate() {
		float scaleWidth = screenWidth / (float) imageWidth;
		float scaleHeight = screenHeight / (float) imageHeight;
		scaleRate = Math.min(scaleWidth, scaleHeight);
	}

	public float getScaleRate() {
		return scaleRate;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			event.startTracking();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
			if (getScale() > 1.0f) {
				// If we're zoomed in, pressing Back jumps out to show the
				// entire image, otherwise Back returns the user to the gallery.
				zoomTo(1.0f);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	protected Handler mHandler = new Handler();

	@Override
	public void setImageResource(int resId) {
		// TODO Auto-generated method stub
		super.setImageResource(resId);
		image = BitmapFactory.decodeResource(getResources(), resId);
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		
		startView();
	}


	@Override
	public void setImageDrawable(Drawable drawable) {
		// TODO Auto-generated method stub
		super.setImageDrawable(drawable);
		BitmapDrawable bd = (BitmapDrawable) drawable;
		image = bd.getBitmap();
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		
		startView();
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);
		image = bitmap;
		// 计算适应屏幕的比例		
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		
		startView();
	}
	

	private void startView() {
		Log.d(TAG, "startView");
		arithScaleRate();
		//缩放到屏幕大小
		zoomTo(scaleRate, screenWidth/2, screenHeight/2, 200f);		
		
		Log.d(TAG, "screenWidth:" + screenWidth + " screenHeight:" + screenHeight);
		Log.d(TAG, "imageWidth:" + imageWidth + " imageHeight:" + imageHeight);
		Log.d(TAG, "scaleRate:" + scaleRate);
		
		//居中
		layoutToCenter();
	}

	// Center as much as possible in one or both axis. Centering is
	// defined as follows: if the image is scaled down below the
	// view's dimensions then center it (literally). If the image
	// is scaled larger than the view and is translated out of view
	// then translate it back into view (i.e. eliminate black bars).
	protected void center(boolean horizontal, boolean vertical) {
		// if (mBitmapDisplayed.getBitmap() == null) {
		// return;
		// }
		if (image == null) {
			return;
		}

		Matrix m = getImageViewMatrix();

		RectF rect = new RectF(0, 0, image.getWidth(), image.getHeight());
//		RectF rect = new RectF(0, 0, imageWidth*getScale(), imageHeight*getScale());

		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {
			int viewHeight = getHeight();
			if (height < viewHeight) {
				deltaY = (viewHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < viewHeight) {
				deltaY = getHeight() - rect.bottom;
			}
		}

		if (horizontal) {
			int viewWidth = getWidth();
			if (width < viewWidth) {
				deltaX = (viewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < viewWidth) {
				deltaX = viewWidth - rect.right;
			}
		}

		postTranslate(deltaX, deltaY);
		setImageMatrix(getImageViewMatrix());
	}

	private void init() {
		setScaleType(ImageView.ScaleType.MATRIX);
		
		gestureScanner = new GestureDetector(mContext, new OnImageGestureListener());
		setLongClickable(true);
		this.setOnTouchListener(new OnTouchListener() {

			float baseValue;
			float originalScale;
			
			public boolean onTouch(View view, MotionEvent event) {
				// TODO Auto-generated method stub

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					baseValue = 0;
					originalScale = getScale();
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (event.getPointerCount() == 2) {
						float x = event.getX(0) - event.getX(1);
						float y = event.getY(0) - event.getY(1);
						float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
						// System.out.println("value:" + value);
						if (baseValue == 0) {
							baseValue = value;
						} else {
							float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
							// scale the image
							zoomTo(originalScale * scale, x + event.getX(1), y + event.getY(1));

						}
					}
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// 图片的实时宽，高
					float width = getScale() * getImageWidth();
					float height = getScale() * getImageHeight();

					float v[] = new float[9];
					Matrix m = getImageMatrix();
					m.getValues(v);
					float top = v[Matrix.MTRANS_Y];
					float bottom = top + height;
					float left = v[Matrix.MTRANS_X];
					float right = left + width;

					float translaterX = 0;
					float translaterY = 0;
					
					if ((int) width > screenWidth) {
						if (left > 0) {
							translaterX = -left;
						}
						if (right < screenWidth){
							translaterX = screenWidth - right;
						}
					}
					if ((int) height > screenHeight) {
						if (top > 0) {
							translaterY = -top;
						}
						if (bottom < screenHeight) {
							translaterY = screenHeight - bottom;
						}
					}
					if (translaterX != 0 || translaterY != 0) {
						Log.d(TAG, "translaterX:" + translaterX + " translaterY:" + translaterY);
						postTranslateDur(translaterX, translaterY, 200f);
					}
					
				}
				return gestureScanner.onTouchEvent(event);
			}	
			
		});
          
        gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener(){  
  
            public boolean onDoubleTap(MotionEvent e) {  
                // TODO Auto-generated method stub  
                // 双击时产生一次                      
            	if (getScale() > getScaleRate()) {
            		Log.d(TAG, "onDoubleTap:1");
					zoomTo(getScaleRate(), screenWidth / 2, screenHeight / 2, 200f);
					// imageView.layoutToCenter();
				} else {
					Log.d(TAG, "onDoubleTap:2");
					zoomTo(1.0f, screenWidth / 2, screenHeight / 2, 200f);
				}
                Log.d(TAG, "onDoubleTap");  
                return true;  
            }  
  
            public boolean onDoubleTapEvent(MotionEvent e) {  
                // TODO Auto-generated method stub  
                // 双击时产生两次  
                Log.d(TAG, "onDoubleTapEvent");  
                return false;  
            }  
  
            public boolean onSingleTapConfirmed(MotionEvent e) {  
                //短快的点击算一次单击  
                Log.d(TAG, "onSingleTapConfirmed");  
                return false;    
            }       
        });
	}
	
	private class OnImageGestureListener implements OnGestureListener {

		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onFling:e1:" + e1.getAction() + " e2:" + e2.getAction());
			return false;
		}

		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			// TODO Auto-generated method stub

			// 图片的实时宽，高
			float width, height;
			width = getScale() * getImageWidth();
			height = getScale() * getImageHeight();
			// 一下逻辑为移动图片和滑动gallery换屏的逻辑。如果没对整个框架了解的非常清晰，改动以下的代码前请三思！！！！！！
			if ((int) width <= screenWidth && (int) height <= screenHeight)// 如果图片当前大小<屏幕大小，直接处理滑屏事件
			{
//				super.onScroll(e1, e2, distanceX, distanceY);
			} else {
				if ((int) width <= screenWidth) {
					postTranslate(0, -distanceY);
				}else if ((int) height <= screenHeight) {
					postTranslate(-distanceX, 0);
				}else {
					postTranslate(-distanceX, -distanceY);
				}

			}
//			Log.d(TAG, "onScroll:e1:" + e1.getAction() + " e2:" + e2.getAction());
			return false;
		}

		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onShowPress");
		}

		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onSingleTapUp");
			return false;
		}
    	
    }
	
	/**
	 * 设置图片居中显示
	 */
	public void layoutToCenter()
	{
		//正在显示的图片实际宽高
		float width = imageWidth*getScale();
		float height = imageHeight*getScale();
		
		//空白区域宽高
		float fill_width = screenWidth - width;
		float fill_height = screenHeight - height;
		
		//需要移动的距离
		float tran_width = 0f;
		float tran_height = 0f;
		
		if(fill_width>0)
			tran_width = fill_width/2;
		if(fill_height>0)
			tran_height = fill_height/2;
			
		
		postTranslate(tran_width, tran_height);
		setImageMatrix(getImageViewMatrix());
	}

	protected float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(mMatrixValues);
		mMinZoom = Math.min((screenWidth/2f)/imageWidth, (screenHeight/2f)/imageHeight);
		
		return mMatrixValues[whichValue];
	}

	// Get the scale factor out of the matrix.
	protected float getScale(Matrix matrix) {
		return getValue(matrix, Matrix.MSCALE_X);
	}

	protected float getScale() {
		return getScale(mSuppMatrix);
	}

	// Combine the base matrix and the supp matrix to make the final matrix.
	protected Matrix getImageViewMatrix() {
		// The final matrix is computed as the concatentation of the base matrix
		// and the supplementary matrix.
		mDisplayMatrix.set(mBaseMatrix);
		mDisplayMatrix.postConcat(mSuppMatrix);
		return mDisplayMatrix;
	}

	static final float SCALE_RATE = 1.25F;

	// Sets the maximum zoom, which is a scale relative to the base matrix. It
	// is calculated to show the image at 400% zoom regardless of screen or
	// image orientation. If in the future we decode the full 3 megapixel image,
	// rather than the current 1024x768, this should be changed down to 200%.
	protected float maxZoom() {
		if (image == null) {
			return 1F;
		}

		float fw = (float) image.getWidth() / (float) mThisWidth;
		float fh = (float) image.getHeight() / (float) mThisHeight;
		float max = Math.max(fw, fh) * 4;
		return max;
	}

	protected void zoomTo(float scale, float centerX, float centerY) {
		if (scale > mMaxZoom) {
			scale = mMaxZoom;
		} else if (scale < mMinZoom) {
			scale = mMinZoom;
		}

		float oldScale = getScale();
		float deltaScale = scale / oldScale;

		mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
		setImageMatrix(getImageViewMatrix());
		center(true, true);
	}

	protected void zoomTo(final float scale, final float centerX, final float centerY, final float durationMs) {
		final float incrementPerMs = (scale - getScale()) / durationMs;
		final float oldScale = getScale();
		final long startTime = System.currentTimeMillis();

		mHandler.post(new Runnable() {
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				float target = oldScale + (incrementPerMs * currentMs);
				zoomTo(target, centerX, centerY);
				if (currentMs < durationMs) {
					mHandler.post(this);
				}
			}
		});
	}
	

	protected void zoomTo(float scale) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		zoomTo(scale, cx, cy);
	}

	protected void zoomToPoint(float scale, float pointX, float pointY) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		panBy(cx - pointX, cy - pointY);
		zoomTo(scale, cx, cy);
	}

	protected void zoomIn() {
		zoomIn(SCALE_RATE);
	}

	protected void zoomOut() {
		zoomOut(SCALE_RATE);
	}

	protected void zoomIn(float rate) {
		if (getScale() >= mMaxZoom) {
			return; // Don't let the user zoom into the molecular level.
		} else if (getScale() <= mMinZoom) {
			return;
		}
		if (image == null) {
			return;
		}

		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		mSuppMatrix.postScale(rate, rate, cx, cy);
		setImageMatrix(getImageViewMatrix());
	}

	protected void zoomOut(float rate) {
		if (image == null) {
			return;
		}

		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		// Zoom out to at most 1x.
		Matrix tmp = new Matrix(mSuppMatrix);
		tmp.postScale(1F / rate, 1F / rate, cx, cy);

		if (getScale(tmp) < 1F) {
			mSuppMatrix.setScale(1F, 1F, cx, cy);
		} else {
			mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
		}
		setImageMatrix(getImageViewMatrix());
		center(true, true);
	}

	public void postTranslate(float dx, float dy) {
		mSuppMatrix.postTranslate(dx, dy);
		setImageMatrix(getImageViewMatrix());
	}
	float _dx = 0.0f;
	float _dy = 0.0f;
	protected void postTranslateDur(final float dx, final float dy, final float durationMs) {
		_dx = 0.0f;
		_dy = 0.0f;
		
		final float incrementPerMsX = dx / durationMs;
		final float incrementPerMsY = dy / durationMs;
		final long startTime = System.currentTimeMillis();
		mHandler.post(new Runnable() {
			public void run() {
				long now = System.currentTimeMillis();
				float currentMs = Math.min(durationMs, now - startTime);
				
				postTranslate(incrementPerMsX * currentMs - _dx,
						incrementPerMsY * currentMs - _dy);
				_dx = incrementPerMsX * currentMs;
				_dy = incrementPerMsY * currentMs;

				if (currentMs < durationMs) {
					mHandler.post(this);
				}
			}
		});
	}

	protected void panBy(float dx, float dy) {
		postTranslate(dx, dy);
		setImageMatrix(getImageViewMatrix());
	}
}
