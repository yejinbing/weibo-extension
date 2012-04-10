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

import static com.googlecode.WeiboExtension.EventStream.Constants.EVENT_KEY_EXTRA;
import static com.googlecode.WeiboExtension.EventStream.Constants.FRIEND_KEY_EXTRA;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY_PARAMETER;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.EventstreamIntentData;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.EventstreamIntents;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Listen for broadcasts from Event Stream and launch the SNSSampleService 
 * with mirroring intent and data accordingly.
 */
public class EventStreamListener extends BroadcastReceiver {

    private static final String LOG_TAG = "SNSSample-EventStreamListener";
/**
 * This is the OverRidden onReceive method.
 * It handles the EventStreams intents and forwards them to the Service accordingly.
 */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        if (Config.DEBUG) {
            Log.d(LOG_TAG, "Received broadcast: " + action);
        }
        if (EventstreamIntents.REGISTER_PLUGINS_REQUEST_INTENT
                .equals(action)) {
            serviceIntent = new Intent(Constants.REGISTER_PLUGIN_INTENT);
        }
        else if (intent.hasExtra(PLUGIN_KEY_PARAMETER)
                && PLUGIN_KEY.equals(intent
                        .getStringExtra(PLUGIN_KEY_PARAMETER))) {
            if (Config.DEBUG) {
                Log.d(LOG_TAG, "Plugin key valid: " + intent
                        .getStringExtra(PLUGIN_KEY_PARAMETER));
            }
            if (EventstreamIntents.STATUS_UPDATE_INTENT.equals(action)) {
                if (intent.hasExtra(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE)) {
                    serviceIntent = new Intent(Constants.SEND_STATUS_UPDATE_INTENT);
                    serviceIntent.putExtra(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE, intent
                            .getStringExtra(EventstreamIntentData.EXTRA_STATUS_UPDATE_MESSAGE));
                }

            } else if (EventstreamIntents.REFRESH_REQUEST_INTENT.equals(action)) {
                serviceIntent = new Intent(Constants.REFRESH_REQUEST_INTENT);

            } else if (EventstreamIntents.VIEW_EVENT_INTENT.equals(action)) {
                String eventKey = intent.getStringExtra(EventStreamConstants.EventstreamIntentData.EXTRA_EVENT_KEY);
                String friendKey = intent.getStringExtra(EventStreamConstants.EventstreamIntentData.EXTRA_FRIEND_KEY);
                
                serviceIntent = new Intent(Constants.LAUNCH_BROWSER_INTENT);
                serviceIntent.putExtra(EVENT_KEY_EXTRA, eventKey);
                serviceIntent.putExtra(FRIEND_KEY_EXTRA, friendKey);
            }
        }
        else {
            if (Config.DEBUG) {
                Log.d(LOG_TAG, "Invalid plugin key, expected: " + PLUGIN_KEY + " but received :" + intent
                        .getStringExtra(PLUGIN_KEY_PARAMETER));
            }
        }
        if (serviceIntent != null) {
            serviceIntent.setClass(context, SNSSampleService.class);
            serviceIntent.putExtra(PLUGIN_KEY_PARAMETER, PLUGIN_KEY);
            context.startService(serviceIntent);
        }
    }
}
