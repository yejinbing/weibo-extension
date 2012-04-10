/*
 * Copyright 2010 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.googlecode.WeiboExtension.EventStream;

import static com.googlecode.WeiboExtension.EventStream.Constants.AUTHENTICATE_INTENT;
import static com.googlecode.WeiboExtension.EventStream.Constants.LAUNCH_BROWSER_INTENT;
import static com.googlecode.WeiboExtension.EventStream.Constants.LOGOUT_INTENT;
import static com.googlecode.WeiboExtension.EventStream.Constants.LOG_TAG;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY_PARAMETER;
import static com.googlecode.WeiboExtension.EventStream.Constants.REFRESH_REQUEST_INTENT;
import static com.googlecode.WeiboExtension.EventStream.Constants.REGISTER_PLUGIN_INTENT;
import static com.googlecode.WeiboExtension.EventStream.Constants.SEND_STATUS_UPDATE_INTENT;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.SingleWeiboActivity;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.ConfigState;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.EventstreamIntentData;
import com.googlecode.WeiboExtension.EventStream.SNSSamplePluginApplication.State;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
/**
 * This Service is the main worker of actions to be taken. It is started when intents are 
 * received and initates the work on the Service context.
 * 
 *
 */
public class SNSSampleService extends IntentService {
	
	private static final String TAG = "SNSSampleService";
	
	private static EventstreamSnsEngine socialMediaEngine;
	
	public static final String INTENT_ACTION_START
		= "com.googlecode.WeiboExtension.EventStream.intent.action.start";
	public static final String INTENT_ACTION_STOP
	    = "com.googlecode.WeiboExtension.EventStream.intent.action.stop";
	public static final String INTENT_ACTION_NET_CONNECT
		= "com.googlecode.WeiboExtension.EventStream.intent.action.net_connect";
	
	public static final String EXTRA_INTERVAL = "interval";

    public SNSSampleService() {
        super("SNSSampleService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (socialMediaEngine == null) {
        	socialMediaEngine = new EventstreamSnsEngine(getApplicationContext());
        }
    }

    /**
     * The main intent receiver. The intents are then dispatched as work tasks to do
     * by other classes.
     */
    @Override
    public void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
       
        // At this point, all intent should have the plug-in parameter
        String action = intent.getAction();
        if (Config.DEBUG) {
            Log.d(LOG_TAG, "snsSampleService started with " + action);
        }
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (null == am) {
            return;
        }
        Intent i = new Intent(intent);
        i.setAction(REFRESH_REQUEST_INTENT);
        i.putExtra(PLUGIN_KEY_PARAMETER, PLUGIN_KEY);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        
       //网络连接上后，测试是否需要刷新一次微博
        if (INTENT_ACTION_NET_CONNECT.equals(action)) {
        	Settings mSettings = new Settings(getApplicationContext());
        	//判断当前是否有登录账户并设置为自动提醒
        	if (mSettings.isLoggedIn() && mSettings.isAutomaticAlert()) {
    			long interval = mSettings.getInspectionInterval();
    			if (interval != 0) {
    				Log.d(TAG, "alarm start:" + interval);
    				am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
    	                    SystemClock.elapsedRealtime(), interval, pi);
    			}else {
    				Log.d(TAG, "alarm cancel");
    				am.cancel(pi);
    			}
        	}
        }else if (INTENT_ACTION_START.equals(action)) {
        	long interval = intent.getLongExtra(EXTRA_INTERVAL, 0);
        	if (interval != 0) {
        		Log.d(TAG, "alarm start:" + interval);
	            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                    SystemClock.elapsedRealtime(), interval, pi);
        	}else {
        		Log.d(TAG, "alarm cancel");
        		am.cancel(pi);
        	}
        }else if (INTENT_ACTION_STOP.equals(action)) {
        	Log.d(TAG, "alarm cancel");
        	am.cancel(pi);
        }
        
        if (! intent.hasExtra(PLUGIN_KEY_PARAMETER) ||
                ! PLUGIN_KEY.equals(intent.getStringExtra(PLUGIN_KEY_PARAMETER))) {
            if (Config.DEBUG) {
                Log.d(LOG_TAG, "Premature return:" + intent.getStringExtra(PLUGIN_KEY_PARAMETER));
            }
            return;
        }
        
        if (REGISTER_PLUGIN_INTENT.equals(action)) { //Register the plugin to the EventStream
//            socialMediaEngine = new EventstreamSnsEngine(getApplicationContext()); //Create the class simulating the backbone          
            register();
        } else if (REFRESH_REQUEST_INTENT.equals(action)) { //Initiate a refresh
            refresh();
        } else if (LOGOUT_INTENT.equals(action)) { //Initiate a logout
            logout();
        } else if (AUTHENTICATE_INTENT.equals(action)) {//Initiate login
            /*String username = intent.getStringExtra(USERNAME_EXTRA);
            String password = intent.getStringExtra(PASSWORD_EXTRA);

            // Authenticate...
            boolean success = authenticate(username, password);

            if (success) { //If login succeded - refresh.
                refresh();
            }*/
        } else if (SEND_STATUS_UPDATE_INTENT.equals(action)) { //Update MY status.
            String message = intent.getStringExtra(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE);
            updateStatus(message, System.currentTimeMillis());
        } else if (LAUNCH_BROWSER_INTENT.equals(action)) { //Launch the activityview
            String eventKey = intent.getStringExtra(EventStreamConstants.EventstreamIntentData.EXTRA_EVENT_KEY);
            String friendKey = intent.getStringExtra(EventStreamConstants.EventstreamIntentData.EXTRA_FRIEND_KEY);
            launchActivity(getApplicationContext(), eventKey, friendKey);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    /**
     * This method refreshes friends and events 
     */
    private void refresh() {
        refreshSNSSampleFriends(); 
        retrieveLatestPosts();
    }

    /**
     * This method initiates the registration of the application as a plugin
     * to the EventStream 
     */
    private void register() {
        Database database = new Database(getApplicationContext());

        if (! database.isRegistered()) {
            SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());
            if (snsSample.isLoggedIn()) {
                logout();
            }            
        }       
        database.registerPlugin();
        
        Settings settings = new Settings(getApplicationContext());
		if (settings.getOwnId() != 0) {
            // State that we are configured
			 final String databaseName = getString(R.string.ts_snssample_logout_label)
		     		+ " " + settings.getDisplayName();
			database.setConfigurationText(databaseName);
            database.setConfigurationState(ConfigState.CONFIGURED);
            SNSSampleNotification notification = null;
            if (notification == null) {
                notification = new SNSSampleNotification(State.AUTHENTICATION_SUCCESS, null);
            }
            ((SNSSamplePluginApplication) getApplication()).setState(notification);
        }
	
    }

    /**
     * This method launches the Actiivity view (SNSSampleActivity.java) upon request.
     * @param context Context of the service
     * @param databaseEventId Event Id as from Eventstream database
     * @param friendKey Friend ID as from Eventstream database
     */
    protected void launchActivity(Context context, String databaseEventId, String friendKey) {
    	String snsSampleEventId = Database.getEventId(databaseEventId);
    	Intent intent = new Intent(context, SingleWeiboActivity.class);
    	intent.putExtra(SingleWeiboActivity.EXTRA_WEIBO_ID, Long.parseLong(snsSampleEventId));
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(intent);
                        
    }

    /**
     * This method initiates a logout from the Social Network Service
     */
    private void logout() {
        // Delete all snsSample data in Event Stream, remove settings and terminate snsSample objects...
        try {
            //TODO Add this when the OnlineImageCache is in place...
            // ((snsSamplePluginApplication) getApplication()).clearsnsSampleData();

            ((SNSSamplePluginApplication) getApplication()).clearNotification();

            // Clear "local"/internal data
            new Settings(getApplicationContext()).removeSettings();

            // Clear data from event stream framework
            Database database = new Database(getApplicationContext());

            database.reInitialize();

            database.cleanup();
            
            TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(getApplicationContext());
            dbAdapter.cleanup();

            // Re-initialize snsSample object...
            SNSSampleFactory.terminate(true);
        } catch (Exception e) {
            if (Config.DEBUG) {
                Log.e(LOG_TAG, "Error when deleting plugin information. Is Event Stream installed?");
            }
        } finally {
            ((SNSSamplePluginApplication) getApplication()).setState(SNSSamplePluginApplication.State.NOT_CONFIGURED);
            Log.v(LOG_TAG, "Logout Done");
        }
    }
    /**
     * Updates my own status in the service.
     * @param message Status message to set.
     * @param timestamp Timestamp of change.
     */
    private void updateStatus(String message, long timestamp) {
        SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());
        boolean success = false;

        success = snsSample.updateStatus(getApplicationContext(), message);
        if (success) {
            ((SNSSamplePluginApplication) getApplication()).cancelNotifications();
            Database database = new Database(getApplicationContext());

            database.setOwnStatus(message, timestamp);
        }
    }

    /**
     * This function initiates a retrieval of the latest posts from the network 
     * service.
     */
    private void retrieveLatestPosts() {
        Database database = new Database(getApplicationContext());
        SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());

        if (Config.DEBUG) {
        	Log.d(LOG_TAG, "Retrieve latest snsSample statuses.");
        }

        boolean result = snsSample.retrieveLatestPosts(getApplicationContext(),
        		database, database, SNSSampleService.socialMediaEngine);
        if (result){
        	database.storeEvents();
        }
    }

    /**
     * Refresh our local friend list, handle added and removed friends
     */
    private void refreshSNSSampleFriends() {
        Database database = new Database(getApplicationContext());
        SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());

        if (Config.DEBUG) {
        	Log.d(LOG_TAG, "Register friends.");
        }

        snsSample.refreshSNSSampleFriends(getApplicationContext(), 
        		SNSSampleService.socialMediaEngine, database);
        database.storeFriends();
    }

}
