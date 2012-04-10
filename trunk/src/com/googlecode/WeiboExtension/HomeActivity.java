package com.googlecode.WeiboExtension;

import com.googlecode.WeiboExtension.View.TabView;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class HomeActivity extends TabActivity{
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab);
        
        TabHost mTabHost = getTabHost();       

        TabView view;
        view = new TabView(this, R.string.tab_home_page,
        		R.drawable.s_weibo_list, R.drawable.s_weibo_list_selected);
        TabSpec homePageSpec = mTabHost.newTabSpec(getResources().getString(R.string.tab_home_page));
        homePageSpec.setIndicator(view);
        Intent homePageIntent = new Intent(this, FriendsTimeLineActivity.class);
        homePageSpec.setContent(homePageIntent);
        
//        view = new TabView(this, R.string.tab_personal_page,
//        		R.drawable.ic_btn_home_normal, R.drawable.ic_btn_home_pressed);
//        TabSpec personalPageSpec = mTabHost.newTabSpec(getResources().getString(R.string.tab_personal_page));
//        personalPageSpec.setIndicator(view);
//        Intent personalPageIntent = new Intent(this, PublicTimeLineActivity.class);
//        personalPageSpec.setContent(personalPageIntent);
        
        view = new TabView(this, R.string.tab_message_center,
        		R.drawable.s_weibo_message, R.drawable.s_weibo_message_selected);
        TabSpec messageCenterSpec = mTabHost.newTabSpec(getResources().getString(R.string.tab_message_center));
        messageCenterSpec.setIndicator(view);
        Intent messageCenterIntent = new Intent(this, MyMessagesActivity.class);
        messageCenterSpec.setContent(messageCenterIntent);
        
        mTabHost.addTab(homePageSpec);
//        mTabHost.addTab(personalPageSpec);
        mTabHost.addTab(messageCenterSpec);
        mTabHost.setCurrentTab(0);
        
    }

}