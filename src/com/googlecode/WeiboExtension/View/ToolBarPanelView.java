package com.googlecode.WeiboExtension.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ToolBarPanelView extends TableLayout{
	

	private Context mContext;
	private TableRow toolBarContainer;
	
	
	public ToolBarPanelView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public ToolBarPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}
	
	private void init() {
		toolBarContainer = new TableRow(mContext);
		this.setStretchAllColumns(true);
		toolBarContainer.setGravity(Gravity.CENTER_HORIZONTAL);
		this.addView(toolBarContainer);
	}
	
	public void addTool(View view) {
		toolBarContainer.addView(view);
	}

	
}
