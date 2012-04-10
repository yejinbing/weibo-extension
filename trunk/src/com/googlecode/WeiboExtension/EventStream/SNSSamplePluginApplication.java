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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY_PARAMETER;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SNSSamplePluginApplication extends Application {

    public enum State {
        NOT_CONFIGURED,
        NOT_AUTHENTICATED,
        AUTHENTICATION_IN_PROGRESS,
        AUTHENTICATION_FAILED,
        AUTHENTICATION_SUCCESS,
        AUTHENTICATED,
        AUTHENTICATION_BAD_CREDENTIALS,
        AUTHENTICATION_NETWORK_FAILED,
        STATUS_UPDATE_FAILED,
        INVALID_ACCOUNT
    };

    private static final String LOG_TAG = "SNSSamplePluginApplication";

    private static final int UPDATE_NOTIFICATION = 1;

    private static final int AUTH_FAILED_NOTIFICATION = 2;

    private static final int STATUS_UPDATE_FAILED = 3;

    private NotificationHandler mNotificationHandler;

    private CleanUpHandler mCleanupHandler;

    private List<StateListener> mStateListener;

    /** The current state */
    private State mState = State.NOT_CONFIGURED;

    /**
     * Interface for the state listener
     */
    public interface StateListener {
        /**
         * Indicate that the current state has changed
         *
         * @param oldState The old state
         * @param newState The new changed state
         */
        void onStateChange(final SNSSampleNotification newState);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SNSsample snsSample = null;

        mNotificationHandler = new NotificationHandler();
        mCleanupHandler = new CleanUpHandler();
        snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());
        snsSample.setServiceStateListener(mNotificationHandler);
        snsSample.setServiceStateListener(mCleanupHandler);

        synchronized (this) {
            mStateListener = Collections.synchronizedList(new ArrayList<StateListener>());
        }

        if (snsSample.isLoggedIn()) {
            setState(State.AUTHENTICATED);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());

        snsSample.removeServiceStateListener(mNotificationHandler);
        snsSample.removeServiceStateListener(mCleanupHandler);

        SNSSampleFactory.terminate(false);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());
        snsSample.close();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (Config.DEBUG) {
            Log.d(LOG_TAG, "Configuration changed. starting service to update registration.");
        }
        Settings settings = new Settings(this);
        int oldLocaleHash = settings.getLocaleHash();
        if (newConfig != null && newConfig.locale != null) {
            int newLocaleHash = newConfig.locale.hashCode();
            if (oldLocaleHash != newLocaleHash) {
                Intent intent = new Intent(Constants.REGISTER_PLUGIN_INTENT);
                intent.setComponent(new ComponentName(getPackageName(), SNSSampleService.class
                        .getName()));
                intent.putExtra(Constants.PLUGIN_KEY_PARAMETER, Constants.PLUGIN_KEY);
                startService(intent);
                settings.setLocaleHash(newLocaleHash);
            }
        }
    }

    public synchronized void setState(State newState) {
        SNSSampleNotification state = new SNSSampleNotification(newState, null);

        setState(state);
    }

    public synchronized void setState(SNSSampleNotification notification) {
        mState = notification.getState();

        if (mStateListener != null) {
            if (mStateListener.isEmpty()) {
                if (notification.getState() == State.AUTHENTICATION_FAILED
                        || notification.getState() == State.AUTHENTICATION_BAD_CREDENTIALS
                        || notification.getState() == State.STATUS_UPDATE_FAILED) {
                    fireNotification(notification.getState());
                }
            } else {
                for (StateListener listener : mStateListener) {
                    listener.onStateChange(notification);
                }
            }
        }
    }

    public synchronized State getState() {
        return mState;
    }

    public synchronized void addStateListener(StateListener listener) {
        if (listener != null) {
            mStateListener.add(listener);
        }
    }

    public synchronized void removeStateListener(StateListener listener) {
        if (listener != null) {
            mStateListener.remove(listener);
        }
    }

    public void cancelNotifications() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    /**
     * Clear all data associated with the SNSSample.
     */
    public void clearSNSData() {
        // Clear data from event stream framework
        new Database(getApplicationContext()).clearData();

        // Clear "local"/internal data
        new Settings(getApplicationContext()).removeSettings();
    }


    public class NotificationHandler implements SNSsample.ServiceStateChangeListener {
// JHAB Should be removed - I think
        public void onServiceStateChanged(SNSsample.ServiceState oldState,
                SNSsample.ServiceState newState) {
            if (Config.DEBUG) {
                Log.d(LOG_TAG, "Plugin application onservice state change:" + oldState + ":" + newState);
            }

            if (newState == SNSsample.ServiceState.SERVER_OPERATION_IN_PROGRESS) {
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification(
                        R.drawable.notif_data_fetch_ongoing_icn, null, System.currentTimeMillis());
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        SNSSamplePluginApplication.this, 0, null, Intent.FLAG_ACTIVITY_NEW_TASK);
                notification.setLatestEventInfo(SNSSamplePluginApplication.this, getResources()
                        .getText(R.string.ts_snssample_update_notification_title), getResources()
                        .getText(R.string.ts_snssample_update_notification_message), pendingIntent);
                nm.notify(UPDATE_NOTIFICATION, notification);
            } else if (oldState == SNSsample.ServiceState.SERVER_OPERATION_IN_PROGRESS
                    && newState == SNSsample.ServiceState.PASSIVE) {
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(UPDATE_NOTIFICATION);
            }
        }
    }

    /**
     *
     * This callback will be called when the service changes state. When before
     * communicating with the server, the app needs to clean up data if there
     * hasn't been any communication towards the server for 30 days.
     */
    public class CleanUpHandler implements SNSsample.ServiceStateChangeListener {
        public void onServiceStateChanged(SNSsample.ServiceState oldState,
                SNSsample.ServiceState newState) {
            if (newState == SNSsample.ServiceState.AUTHENTICATION_IN_PROGRESS
                    || newState == SNSsample.ServiceState.SERVER_OPERATION_IN_PROGRESS) {
                if (isCleanupNeeded()) {
                    clearSNSData();
                }

                storeLastCommuncationTime();
            }
        }
    }

    /**
     * Store the current time (in milliseconds since epoch).
     */
    private void storeLastCommuncationTime() {
        final long currentTime = getCurrentTime();
        new Settings(getApplicationContext()).setLastCommunicationTime(currentTime);
    }

    /**
     * Get the last time we communicated with the server
     *
     * @return the time, in milliseconds since epoch
     */
    private long getLastCommunicationTime() {
        return new Settings(getApplicationContext()).getLastCommunicationTime();
    }

    /**
     * Get the "current" time in milliseconds. The implement isn't safe as the
     * user, programs etc can change the "current" time. See android
     * documentation for details.
     *
     * @return the current time
     */
    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Thirty (30) days in milliseconds
     */
    private static final int ACCESS_30_DAYS = 30 * 24 * 60 * 60 * 1000;

    /**
     *
     * @return true if we have exceeded 30 days of inactivity, false otherwise
     */
    private boolean isCleanupNeeded() {
        boolean result = false;
        final long currentTime = System.currentTimeMillis();
        final long lastAccess = getLastCommunicationTime();

        result = (lastAccess != 0 && lastAccess <= currentTime && (lastAccess + ACCESS_30_DAYS >= currentTime));

        if (Config.DEBUG) {
            Log.d(LOG_TAG, "SNSSample clean up; lastAccess:" + lastAccess + " currentTime:"
                    + currentTime + " result:" + result);
        }

        return result;
    }

    public synchronized void fireNotification(State state) {
//JHAB Should be considered to be removed    	
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification(R.drawable.notif_data_fetch_failed_icn, null, System
                .currentTimeMillis());

        if (state == State.AUTHENTICATION_FAILED || state == State.AUTHENTICATION_BAD_CREDENTIALS) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this, SNSSamplePluginConfig.class));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            notif.setLatestEventInfo(this, getResources().getText(
                    R.string.ts_snssample_auth_failed_notification_title), getResources().getText(
                    R.string.ts_snssample_auth_failed_notification_message), pendingIntent);
            nm.notify(AUTH_FAILED_NOTIFICATION, notif);
            // Clear all data and change state to logged out
            Intent logoutIntent = new Intent(Constants.LOGOUT_INTENT);

            logoutIntent.setClass(getApplicationContext(), SNSSampleService.class);
            logoutIntent.putExtra(PLUGIN_KEY_PARAMETER, PLUGIN_KEY);
            startService(logoutIntent);
        } else if (state == State.STATUS_UPDATE_FAILED) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, null, 0);
            notif.setLatestEventInfo(this, getResources().getString(
                    R.string.ts_snssample_update_failed_notification_title), getResources()
                    .getString(R.string.ts_snssample_connection_failed), pendingIntent);
            nm.notify(STATUS_UPDATE_FAILED, notif);
        }
    }

    public synchronized void clearNotification() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        nm.cancel(STATUS_UPDATE_FAILED);
        nm.cancel(AUTH_FAILED_NOTIFICATION);
    }
}
