package com.googlecode.WeiboExtension;

import java.util.ArrayList;
import java.util.List;
import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.TitleProvider;
import org.taptwo.android.widget.ViewFlow;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class MyMessagesActivity extends Activity{
	
	private static final String TAG = "MyMessagesActivity";
	
	private Context currentContext = MyMessagesActivity.this;
	
	public static final int DIALOG_EXIT = 0;
	
	private MyMentionsListView myMentionsListView;
	private CommentsToMeListView commentsToMeListView;
	private String[] titles;
	private List<View> mListViews;
	
	private ViewFlow mViewFlow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_message);
		
		titles = getResources().getStringArray(R.array.my_messages_title);
		myMentionsListView = new MyMentionsListView(currentContext);
		commentsToMeListView = new CommentsToMeListView(currentContext);
		mListViews = new ArrayList<View>();
		mListViews.add(myMentionsListView);
		mListViews.add(commentsToMeListView);
		
		mViewFlow = (ViewFlow)findViewById(R.id.my_message_viewflow);
		MsgPagerAdapter adapter = new MsgPagerAdapter(currentContext, titles, mListViews);
		mViewFlow.setAdapter(adapter);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.my_message_viewflowindic);
		indicator.setTitleProvider(adapter);
		mViewFlow.setFlowIndicator(indicator);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				showDialog(DIALOG_EXIT);
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		switch (id) {
			case DIALOG_EXIT:
				return setupExitDialog();
			default:
				return super.onCreateDialog(id, args);
		}
		
	}
	
	 /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private Dialog setupExitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.exit_dialog_msg);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	finish();
				System.exit(0);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        return dialog.create();
    }

	private class MsgPagerAdapter extends BaseAdapter implements TitleProvider {

		private String[] titles;
		private List<View> mListViews;				

		public MsgPagerAdapter(Context context, String[] titles, List<View> listViews) {
			this.titles = titles;
			this.mListViews = listViews;
		}
		
		public int getCount() {
			return titles.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position; 
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return mListViews.get(position);
		}

		/* (non-Javadoc)
		 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
		 */
		public String getTitle(int position) {
			return titles[position];
		}

	}

}
