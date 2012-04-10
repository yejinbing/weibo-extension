package com.googlecode.WeiboExtension.View;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.WeiboExtension.R;

public  class ToolBarView extends LinearLayout implements OnClickListener{
	
	private ImageView imageView;
	private TextView textView;
	
	private int stateFalseDrawable;
	private int stateTrueDrawable;
	
	private boolean state = false;
	
	public ToolBarView(Context c, int title, int drawable, int drawableselec) {
		super(c);
		this.setOrientation(VERTICAL);
		this.setGravity(Gravity.CENTER);
		StateListDrawable backGroundDrawable = new StateListDrawable();
		backGroundDrawable.addState(
				new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, 
				this.getResources().getDrawable(R.drawable.toolbarpanel_item_pressed_bg));
		backGroundDrawable.addState(
				new int[] { android.R.attr.state_enabled },
				this.getResources().getDrawable(R.drawable.toolbarpanel_item_bg));
		this.setBackgroundDrawable(backGroundDrawable);
		this.setEnabled(true);
		this.setClickable(true);
		this.setFocusable(true);
		this.setOnClickListener(this);
		
		imageView = new ImageView(c);			
		imageView.setImageDrawable(this.getResources().getDrawable(drawable));
		addView(imageView);
		textView = new TextView(c);
		textView.setText(title);
		textView.setTextColor(0xFFFFFFFF);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		addView(textView);
		
		stateFalseDrawable = drawable;
		stateTrueDrawable = drawableselec;
	}
	
	public void setImageState(boolean state) {
		this.state = state;
		if (state) {
			imageView.setImageResource(stateTrueDrawable);
		}else {
			imageView.setImageResource(stateFalseDrawable);
		}
	}
	
	public boolean getImageState() {
		return state;
	}

	public void onClick(View view) {
		// TODO Auto-generated method stub
		

	}
}
