package com.googlecode.WeiboExtension.View;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.googlecode.WeiboExtension.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class GifView extends View {
    
    public static final int 		IMAGE_TYPE_UNKNOWN = 0;
	public static final int 		IMAGE_TYPE_STATIC = 1;
	public static final int 		IMAGE_TYPE_DYNAMIC = 2;

	public static final int 		DECODE_STATUS_UNINIT = -1;
	public static final int 		DECODE_STATUS_UNDECODE = 0;
	public static final int 		DECODE_STATUS_DECODING = 1;
	public static final int 		DECODE_STATUS_DECODED = 2;
	
	private ScaleType				mScaleType;
	private boolean 				mAdjustViewBounds	= false;
	private int 					mMaxWidth			= Integer.MAX_VALUE;
	private int						mMaxHeight			= Integer.MAX_VALUE;
	
	private Bitmap 					bitmap;

	public int imageType = IMAGE_TYPE_UNKNOWN;
	public int decodeStatus = DECODE_STATUS_UNDECODE;

	private int width;
	private int height;

	private int resId;
	
	private byte[] bytes;

	private boolean playFlag = false;
   
    private Movie mMovie;   
    private long mMovieStart;
    
    private static final ScaleType[] sScaleTypeArray	= {
        ScaleType.MATRIX,
        ScaleType.FIT_XY,
        ScaleType.FIT_START,
        ScaleType.FIT_CENTER,
        ScaleType.FIT_END,
        ScaleType.CENTER,
        ScaleType.CENTER_CROP,
        ScaleType.CENTER_INSIDE
    };
   
    public GifView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub      
        mMovie = null;
        mMovieStart=0;
        decodeStatus = DECODE_STATUS_UNINIT;
    }
   
    public GifView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub      
        this(context, attrs, 0);
    }
    
    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
       
        mMovie = null;
        mMovieStart=0;
        
        decodeStatus = DECODE_STATUS_UNINIT;
       
        //从描述文件中读出gif的值，创建出Movie实例
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GifView);
       
        int srcID = a.getResourceId(R.styleable.GifView_android_src, 0);
        if(srcID > 0){
        	this.resId = srcID;
        	requestLayout();
            invalidate();
        }
        
        setAdjustViewBounds(
                a.getBoolean(R.styleable.GifView_android_adjustViewBounds, false));
        
        setMaxWidth(a.getDimensionPixelSize(
                R.styleable.GifView_android_maxWidth, Integer.MAX_VALUE));
        
        setMaxHeight(a.getDimensionPixelSize(
                R.styleable.GifView_android_maxHeight, Integer.MAX_VALUE));
        
       
        a.recycle();
    }
    
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mAdjustViewBounds = adjustViewBounds;
        if (adjustViewBounds) {
            setScaleType(ScaleType.FIT_CENTER);
        }
    }
    
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }
    
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }
    
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            setWillNotCacheDrawing(mScaleType == ScaleType.CENTER);            

            requestLayout();
            invalidate();
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;
        
        // Desired aspect ratio of the view's contents (not including padding)
        float desiredAspect = 0.0f;
        
        // We are allowed to change the view's width
        boolean resizeWidth = false;
        
        // We are allowed to change the view's height
        boolean resizeHeight = false;
        
        if (bitmap == null) {
            // If no drawable, its intrinsic size is 0.
            width = -1;
            height = -1;
            w = h = 0;
        } else {
            w = width;
            h = height;
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;
            
            // We are supposed to adjust view bounds to match the aspect
            // ratio of our drawable. See if that is possible.
            if (mAdjustViewBounds) {
                
                int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
                
                resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
                resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
                
                desiredAspect = (float)w/(float)h;
            }
        }
        
        int pleft = super.getPaddingLeft();
        int pright = super.getPaddingRight();
        int ptop = super.getPaddingTop();
        int pbottom = super.getPaddingBottom();

        int widthSize;
        int heightSize;

        if (resizeWidth || resizeHeight) {
            /* If we get here, it means we want to resize to match the
                drawables aspect ratio, and we have the freedom to change at
                least one dimension. 
            */

            // Get the max possible width given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright,
                                                 mMaxWidth, widthMeasureSpec);
            
            // Get the max possible height given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom,
                                                mMaxHeight, heightMeasureSpec);
            
            if (desiredAspect != 0.0f) {
                // See what our actual aspect ratio is
                float actualAspect = (float)(widthSize - pleft - pright) /
                                        (heightSize - ptop - pbottom);
                
                if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                    
                    boolean done = false;
                    
                    // Try adjusting width to be proportional to height
                    if (resizeWidth) {
                        int newWidth = (int)(desiredAspect *
                                            (heightSize - ptop - pbottom))
                                            + pleft + pright;
                        if (newWidth <= widthSize) {
                            widthSize = newWidth;
                            done = true;
                        } 
                    }
                    
                    // Try adjusting height to be proportional to width
                    if (!done && resizeHeight) {
                        int newHeight = (int)((widthSize - pleft - pright)
                                            / desiredAspect) + ptop + pbottom;
                        if (newHeight <= heightSize) {
                            heightSize = newHeight;
                        } 
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright;
            h += ptop + pbottom;
                
            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            widthSize = resolveSize(w, widthMeasureSpec);
            heightSize = resolveSize(h, heightMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }
    
    private int resolveAdjustedSize(int desiredSize, int maxSize,
            int measureSpec) {
    	int result = desiredSize;
    	int specMode = MeasureSpec.getMode(measureSpec);
    	int specSize =  MeasureSpec.getSize(measureSpec);
    	switch (specMode) {
			case MeasureSpec.UNSPECIFIED:
				/* Parent says we can be as big as we want. Just don't be larger
				than max size imposed on ourselves.
				*/
				result = Math.min(desiredSize, maxSize);
				break;
			case MeasureSpec.AT_MOST:
				// Parent says we can be as big as we want, up to specSize. 
				// Don't be larger than specSize, and don't be larger than 
				// the max size imposed on ourselves.
				result = Math.min(Math.min(desiredSize, specSize), maxSize);
				break;
			case MeasureSpec.EXACTLY:
				// No choice. Do what we are told.
				result = specSize;
				break;
		}
		return result;
	}
    
    private InputStream getInputStream() {
		if (resId > 0)
			return getContext().getResources().openRawResource(resId);
		if (bytes != null) {
			return new ByteArrayInputStream(bytes);
		}
		return null;
	}
    
    /**
	 * set gif resource id
	 * 
	 * @param resId
	 */
	public void setGif(int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
		setGif(resId, bitmap);
	}
	
	public void setGif(byte[] bytes) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		setGif(bytes, bitmap);
	}

	/**
	 * set gif resource id and cache image
	 * 
	 * @param resId
	 * @param cacheImage
	 */
	public void setGif(int resId, Bitmap cacheImage) {
		this.bytes = null;
		this.resId = resId;
		imageType = IMAGE_TYPE_UNKNOWN;
		decodeStatus = DECODE_STATUS_UNDECODE;
		playFlag = false;
		bitmap = cacheImage;
		mMovie = Movie.decodeStream(getInputStream());
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		setLayoutParams(new LayoutParams(width, height));
	}
	
	public void setGif(byte[] bytes, Bitmap cacheImage) {
		this.resId = 0;
		this.bytes = bytes;
		imageType = IMAGE_TYPE_UNKNOWN;
		decodeStatus = DECODE_STATUS_UNDECODE;
		playFlag = false;
		bitmap = cacheImage;
		mMovie = Movie.decodeStream(getInputStream());
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		setLayoutParams(new LayoutParams(width, height));
	}
	
	private String getFormat(byte[] image) {
        String PicFormat = "UNKNOWN";
       
        StringBuffer sb = new StringBuffer();
        int tmp = 0;
        for (int i = 0; i < 16;i++) {
            tmp = image[i];
            if(tmp >= 32 && tmp <= 127) {
                sb.append((char)tmp);
            }
        }
        String head = sb.toString();
        if(head.toUpperCase().startsWith("GIF")) {
            PicFormat = "GIF";
        } else if(head.toUpperCase().startsWith("JFIF")) {
            PicFormat = "JPG";
        } else if(head.toUpperCase().startsWith("PNG")) {
            PicFormat = "PNG";
        } else if(head.toUpperCase().startsWith("BM")) {
            PicFormat = "BMP";
        }
        
        return PicFormat;
    }

	private void decode() {

		decodeStatus = DECODE_STATUS_DECODING;
		
		String type = getFormat(bytes);
		
		if (type.equals("GIF")) {
			imageType = IMAGE_TYPE_DYNAMIC;
		} else if (type.equals("JPG")) {
			imageType = IMAGE_TYPE_STATIC;
		} else {
			imageType = IMAGE_TYPE_UNKNOWN;
		}
		
		decodeStatus = DECODE_STATUS_DECODED;
	}
    
    //主要的工作是重载onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
    	
    	if (decodeStatus == DECODE_STATUS_UNINIT) {
			return;
    	} else if (decodeStatus == DECODE_STATUS_UNDECODE) {
			canvas.drawBitmap(bitmap, 0, 0, null);
			if (playFlag) {
				decode();
				invalidate();
			}
		}  else if (decodeStatus == DECODE_STATUS_DECODING) {
			canvas.drawBitmap(bitmap, 0, 0, null);
			invalidate();
		}  else if (decodeStatus == DECODE_STATUS_DECODED) {
			if (imageType == IMAGE_TYPE_STATIC) {
				canvas.drawBitmap(bitmap, 0, 0, null);
			} else if (imageType == IMAGE_TYPE_DYNAMIC) {
		        //当前时间
		        long now = android.os.SystemClock.uptimeMillis();
		        //如果第一帧，记录起始时间
		        if (mMovieStart == 0) {   // first time
		              mMovieStart = now;
		        }
		        if (mMovie != null) {
		                  //取出动画的时长
		            int dur = mMovie.duration();
		            if (dur == 0) {
		            	dur = 1000;
		            }
		                  //算出需要显示第几帧
		            int relTime = (int)((now - mMovieStart) % dur);
		         
		             //设置要显示的帧，绘制即可
		            mMovie.setTime(relTime);
		            mMovie.draw(canvas, 0, 0);
		            invalidate();
		        }       
			} else {
				canvas.drawBitmap(bitmap, 0, 0, null);
			}
		}
    }
    
    public void play() {
		playFlag = true;
		invalidate();
	}

	public void pause() {
		playFlag = false;
		invalidate();
	}

	public void stop() {
		playFlag = false;
		invalidate();
	}
       
}