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
/**
 * This is a dummy class simulating a Social Media backbone service.
 * This class does not use any network connections.
 */
import java.net.URI;
import java.net.URISyntaxException;
import sinaweibo4android.api.Status;
import com.googlecode.WeiboExtension.EventStream.SNSsample.EventStorage;
import com.googlecode.WeiboExtension.EventStream.SNSsample.FriendStorage;
import android.content.Context;
import android.util.Log;

public class EventstreamSnsEngine{
	
	private static Context ctxt;
    private static final String TAG = "FetchService";


    //-- --//
    /**
     * The constructor. Creating class context is needed.
     */
	public EventstreamSnsEngine(Context ctxt) {
        EventstreamSnsEngine.ctxt = ctxt;
	}

	/**
	 * Adds  numOfEvents events randomly to the evenstream DB and to the Backbone content provider.
	 * @param database The database to write the events to
	 * @param numOfEvents Number of events to add.
	 * @param mFriendStore The friend storage - used to control that we are friends - if this was a 
	 * real SNS app it would be needed.
	 * 
	 */
	
	public void insertEvents(Context context, 
			EventStorage eventStorage, FriendStorage friendStorage, Status status) {
		String friend = status.getUser().getScreenName();
		String msg = status.getText();
		URI avatarUrl = null;
		try {
			avatarUrl = status.getUser().getProfileImageURL().toURI();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long userId = status.getUser().getId();
		if (friendStorage.isFriend(userId + "")){
			Log.v("Service", "We are friends");
		} else {
			Log.v("Service", "Fishy fishy friends");					
			Database database = new Database(context);
			database.addFriend(userId + "", 
					avatarUrl.toString(), 
					status.getUser().getName(), null);
			database.storeFriends();
		}
		eventStorage.addEvent(status.getId() + "", msg, friend, 
				avatarUrl.toString(), userId + "", status.getCreatedAt().getTime(),
				com.googlecode.WeiboExtension.EventStream.SNSsample.EventStorage.EventType.STATUS_EVENT );
    }
	
}
