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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;
import sinaweibo4android.AccessToken;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Status;
import sinaweibo4android.api.User;
import sinaweibo4android.api.UserWapper;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SNSsample {
     private static final String LOG_TAG = "SNSSample class";
    /**
     * Exception to indicate a failure in the communication
     * with the server
     */
    static public class SNSSampleException extends Exception {

        /**
		 * 
		 */
		private static final long serialVersionUID = -6835653754183508701L;

		/**
         * Failure to authenticate
         */
        public static final int AUTHENTICATION_FAILED = 0;

        /**
         * Invalid credentials provided by the user
         */
        public static final int AUTHENTICATION_INVALID_CREDENTIALS = 1;

        public int cause;
        public int httpResponseCode;

        public SNSSampleException(int cause) {
            this.cause = cause;
        }

        public SNSSampleException(int cause, int httpResponseCode) {
            this.cause = cause;
            this.httpResponseCode = httpResponseCode;
        }

        public String toString() {
            return "cause=" + cause + " httpResponseCode=" + httpResponseCode;
        }
    }

    public enum ServiceState {
        AUTHENTICATION_IN_PROGRESS,
        SERVER_OPERATION_IN_PROGRESS,
        PASSIVE;
    }

    /**
     * Interface for storing different kinds of events
     */
    public interface EventStorage {
        enum EventType {
            LINK_EVENT,
            STATUS_EVENT,
            PHOTO_EVENT,
            MESSAGE_EVENT
        };
        public void setOwnStatus(String message, long timestamp);
        public void addEvent(String eventId, String message, String title, String picture,
                String friendId, long timestamp, EventType eventType);
    }

    /**
     * Interface for storing friends
     */
    public interface FriendStorage {
        public Set<String> getFriendIds();
        public void removeFriend(String friendId);
        public boolean isFriend(String friendId);
        public void addFriend(String friendId, String profileImageUri, String friendName, Uri uri);
    }

    /**
     * Callback for state changes
     */
    public interface ServiceStateChangeListener {
        void onServiceStateChanged(ServiceState oldState, ServiceState newState);
    }

    public interface Settings {
        public long getOwnId();
        public void setOwnId(long id);
        public String getToken();
        public void setToken(final String token);
        public String getTokenSecret();
        public void setTokenSecret(final String TokenSecret);
    }

    private ServiceState mState;
    private Set<ServiceStateChangeListener> mListeners;
    private Settings mSettings;
    private boolean firstRefreshAfterLoginFlag;

    public SNSsample(Settings settings) {
        mListeners = new HashSet<ServiceStateChangeListener>();
        mSettings = settings;
        setServiceState(ServiceState.PASSIVE);
    }

    public void initialize() {
        setServiceState(ServiceState.PASSIVE);
    }

    public synchronized boolean isLoggedIn() {
        String accessToken = mSettings.getToken();

        return (accessToken != null);
    }

    public synchronized SNSsample.ServiceState getState() {
        return mState;
    }
   
   public void authenticate(long id, String accessToken, String tokenSecret){
       mSettings.setOwnId(id);
       mSettings.setToken(accessToken);
       mSettings.setTokenSecret(tokenSecret);
       firstRefreshAfterLoginFlag = true; 
   }

    public boolean updateStatus(Context context, String message){
//This is merely to simulate that it may be wise to communicate with
// a server prior to update status in own DataBase
    	boolean result = false;
    	Weibo weibo = Weibo.getInstance();
    	AccessToken accessToken = new AccessToken(mSettings.getToken(), mSettings.getTokenSecret());
    	weibo.setAccessToken(accessToken);
    	Status status = null;
		try {
			status = weibo.update(context, message);
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (status != null) {
    		result = true;
    	}else {
    		result = false;
    	}
        return result;
    }
/**
 * 
 * @param eventStorage Interface to the Eventstream database
 * @param friendStorage Interface to the Eventstream database
 * @param snsEngine Interface to the backbone handler.
 * @return
 */
    public boolean retrieveLatestPosts(Context context, 
    		EventStorage eventStorage, FriendStorage friendStorage, EventstreamSnsEngine snsEngine){
        boolean result = false;

        if (snsEngine == null){
        	Log.v(LOG_TAG,"Service not available");
        	return false;
        }
        if (Config.DEBUG) {
            Log.d(LOG_TAG, "Retrieve latest status.");
        }

        if (! isLoggedIn()) {
        	Log.v(LOG_TAG,"UserNotLoggedIn");
            return false;
        }
        Log.v(LOG_TAG,"Adding Events");
        
        Weibo weibo = Weibo.getInstance();
        AccessToken accessToken = new AccessToken(mSettings.getToken(), 
        		mSettings.getTokenSecret());
        weibo.setAccessToken(accessToken);
        
        List<Status> friendsTimeLine = null;
        TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(context);
        Cursor cursor = null;
        long sinceId = 0;
        try {
	        cursor = dbAdapter.queryStatus();
	        while (cursor.moveToNext()) {
	        	long id = cursor.getLong(cursor.getColumnIndex(TimeLineDBAdapter.KEY_ID));
	        	if (id > sinceId) {
	        		sinceId = id;
	        	}
	        }
        }finally {
        	if (cursor != null) {
        		cursor.close();
        	}
        }
        
        try {
        	if (sinceId != 0) {
	        	friendsTimeLine = weibo.getFriendsTimeline(context, sinceId);
	        }else {
	        	friendsTimeLine = weibo.getFriendsTimeline(context);
	        }
        	
        	for (Status timeLine : friendsTimeLine) {
        		dbAdapter.insertStatus(timeLine);
        		snsEngine.insertEvents(context, eventStorage, friendStorage, timeLine);
			}
        	Utility.displayToast(context, friendsTimeLine.size()
        			+ context.getResources().getString(R.string.new_microblogging));
        }catch (WeiboException e) {
        	e.printStackTrace();
        }
        
        result = true;
        return result;
    }
    
    /**
     * Refresh our local friend list, handle added and removed friends
     *
     */
    public boolean refreshSNSSampleFriends(Context context, 
    		EventstreamSnsEngine snsEngine, FriendStorage storage){
        boolean result = false;

        if (! isLoggedIn()) {
            return result;
        }
        if (firstRefreshAfterLoginFlag){ // If first refreesh - please add a number of new friends
        	Set<String> localIds = storage.getFriendIds();
        	Set<String> serverIds = new HashSet<String>();

        	localIds.removeAll(serverIds);
        	for (String friendId : localIds) {
        		storage.removeFriend(friendId);
        	}
        	
        	Weibo weibo = Weibo.getInstance();
            AccessToken accessToken = new AccessToken(mSettings.getToken(), 
            		mSettings.getTokenSecret());
            weibo.setAccessToken(accessToken);
        	
        	List<User> friendsList = new ArrayList<User>();
    		UserWapper friendsWapper = null;
    		long userId = mSettings.getOwnId();
    		int nextCursor = 0;    		
			do {
				try {
    				friendsWapper = weibo.getFriendsWapper(context, userId, nextCursor);
    				friendsList.addAll(friendsWapper.getUsers());
    				nextCursor = friendsWapper.getNextCursor();
				} catch (WeiboException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
			}while (nextCursor != 0);		  		
    		
    		for (User friend : friendsList) {		
				try {
					storage.addFriend(friend.getId() + "", 
							friend.getProfileImageURL().toURI().toString(), 
							friend.getName(), null);
					Log.d(LOG_TAG, "user id:" + friend.getId());
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        	Log.v(SNSsample.LOG_TAG,"FILL_FRIENDS ordered");
        	setServiceState(ServiceState.PASSIVE);//Not entirely true.....
        	firstRefreshAfterLoginFlag = false;
        	result = true;
        } else{  //Has been logged in - no flooding of database
        	result = true;
        }
        	
        return result;
        
    }

    public synchronized void setServiceStateListener(ServiceStateChangeListener callback) {
        if (callback != null) {
            mListeners.add(callback);
        }
    }

    public synchronized void removeServiceStateListener(ServiceStateChangeListener callback) {
        if (callback != null) {
            mListeners.remove(callback);
        }
    }
/**
 * Dummy function to show potential factory usage.
 */
    public void shutdown() {
    }
    
    /**
     * Dummy function to show potential factory usage.
     */
    public void close() {
    }

    private synchronized void setServiceState(ServiceState state) {
        if (mState != state) {
            ServiceState oldState = mState;

            mState = state;
            for (ServiceStateChangeListener l : mListeners) {
                l.onServiceStateChanged(oldState, mState);
            }
        }
    }

 

}
