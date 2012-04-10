package com.googlecode.WeiboExtension.View;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public  class TabView extends LinearLayout {
	
	ImageView imageView;
	TextView textView;
	
	public TabView(Context c, int title, int drawable, int drawableselec) {
		super(c);
		this.setOrientation(VERTICAL);
		imageView = new ImageView(c);
		StateListDrawable listDrawable = new StateListDrawable();
		listDrawable.addState(SELECTED_STATE_SET, this.getResources()
				.getDrawable(drawableselec));
		listDrawable.addState(ENABLED_STATE_SET, this.getResources()
				.getDrawable(drawable));
		imageView.setImageDrawable(listDrawable);
		imageView.setBackgroundColor(Color.TRANSPARENT);
		addView(imageView);
		textView = new TextView(c);
		textView.setText(title);
		textView.setTextColor(0xFFFFFFFF);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		addView(textView);
	}
}
