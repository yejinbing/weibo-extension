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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.googlecode.WeiboExtension.R;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.ConfigState;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.EventTable;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.FriendTable;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.PluginTable;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.SourceTable;
import  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.StatusSupport;
import  com.googlecode.WeiboExtension.EventStream.SNSsample.EventStorage;
import  com.googlecode.WeiboExtension.EventStream.SNSsample.FriendStorage;
import static  com.googlecode.WeiboExtension.EventStream.Constants.LOG_TAG;
import static  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.EVENT_PROVIDER_URI;
import static  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.FRIEND_PROVIDER_URI;
import static  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.PLUGIN_PROVIDER_URI;
import static  com.googlecode.WeiboExtension.EventStream.EventStreamConstants.SOURCE_PROVIDER_URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * This is the file that handles the inserting of events and friends to 
 * the EventStream engine.
 *
 */
public class Database implements FriendStorage, EventStorage {
    private Context mContext;

    private ContentResolver mCr;

    private List<ContentValues> mContentValuesList = new ArrayList<ContentValues>();

    private Set<String> mRegisteredFriends = null;

    private long mSourceId = -1;

    /**
     * The event_key field in the event table is used differently for the
     * events that we support. The reason for this is that we need more data
     * than the object ID to handle deep linking.
     *
     * LINK
     * < friend ID>_<object ID>#<URL encoded link address>;LINK
     *
     * STATUS
     * < friend ID>_<object ID>;STATUS
     *
     * PHOTO
     * < friend ID>_<object ID>;PHOTO
     *
     * MESSAGE
     * < friend ID>_<object ID>;MESSAGE
     */
    
    /**
     * Below strings are used to build delimitters when your application information 
     * is passed to event stream.
     */
    private static final String EVENT_ID_DELIMITER = ";";

    private static final String EVENT_TYPE_LINK_AS_STRING = "LINK";

    private static final String EVENT_TYPE_STATUS_AS_STRING = "STATUS";

    private static final String EVENT_TYPE_PHOTO_AS_STRING = "PHOTO";

    private static final String EVENT_TYPE_MESSAGE_AS_STRING = "MESSAGE";
    private static final String FRIEND_ID_FACBOOK_OBJECT_ID_DELIMITER = "_";

    public Database(Context context) {
        mContext = context;
        mCr = context.getContentResolver();
    }

    /**
     * 
     * @return true if plugin is registered in EventStream content provider.
     */
    public boolean isRegistered() {
        Cursor cursor = null;
        boolean result = false;

        try {
            cursor = mCr.query(PLUGIN_PROVIDER_URI, null, null, null, null);
            result = (cursor != null && cursor.moveToFirst());
        } catch (Exception exception) {
            result = false;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return result;
    }
/***
 * Re-initializes the plugin to the EventStream.
 */
    public void reInitialize() {
        // Reset source
        ContentValues sourceValues = new ContentValues();

        sourceValues.put(SourceTable.ENABLED, 1);
        sourceValues.putNull(SourceTable.CURRENT_STATUS);
        sourceValues.putNull(SourceTable.STATUS_TIMESTAMP);
        mCr.update(SOURCE_PROVIDER_URI, sourceValues, null, null);

        // Reset plugin
        ContentValues pluginValues = new ContentValues();

        pluginValues.put(PluginTable.CONFIGURATION_STATE, ConfigState.NOT_CONFIGURED);
        pluginValues.put(PluginTable.CONFIGURATION_TEXT, mContext.getResources().getString(
                R.string.snssample_register_txt));
        mCr.update(PLUGIN_PROVIDER_URI, pluginValues, null, null);
    }

/**
 * This function cleans upp all earlier entries in the EventStream.
 */
    public void cleanup() {
        // Clear friend
        mCr.delete(FRIEND_PROVIDER_URI, null, null);

        // Clear event
        mCr.delete(EVENT_PROVIDER_URI, null, null);
    }
/**
 * This function clears all data for this extension.
 */
    public void clearData() {
        // Clear friend
        mCr.delete(FRIEND_PROVIDER_URI, null, null);

        // Clear event
        mCr.delete(EVENT_PROVIDER_URI, null, null);

        // Reset source
        ContentValues sourceValues = new ContentValues();

        sourceValues.put(SourceTable.ENABLED, 1);
        sourceValues.putNull(SourceTable.CURRENT_STATUS);
        sourceValues.putNull(SourceTable.STATUS_TIMESTAMP);
        mCr.update(SOURCE_PROVIDER_URI, sourceValues, null, null);

        // Reset plugin
        ContentValues pluginValues = new ContentValues();

        pluginValues.put(PluginTable.CONFIGURATION_STATE, ConfigState.NOT_CONFIGURED);
        pluginValues.put(PluginTable.CONFIGURATION_TEXT, mContext.getResources().getString(
                R.string.snssample_register_txt));
        mCr.update(PLUGIN_PROVIDER_URI, pluginValues, null, null);
    }

    /***
     * This function registers the extension as a plugin 
     * and also registers its source to use.
     * @return the source ID that this extension is using in the EventStream
     */
    public long registerPlugin() {
        Settings settings = new Settings(mContext);
        ContentValues pluginValues = new ContentValues();
        long sourceId = 0;
        pluginValues.put(PluginTable.PLUGIN_KEY, Constants.PLUGIN_KEY);
        pluginValues.put(PluginTable.NAME, mContext.getResources().getString(R.string.ts_snssample_service_name));
        pluginValues.put(PluginTable.STATUS_TEXT_MAX_LENGTH, Constants.STATUS_TEXT_MAX_LENGTH);
        pluginValues.put(PluginTable.ICON_URI,
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.drawable.sina_icon);
        pluginValues.put(PluginTable.API_VERSION, 1);
        ComponentName componentName = new ComponentName(mContext, SNSSamplePluginConfig.class);
        pluginValues.put(PluginTable.CONFIGURATION_ACTIVITY, componentName.flattenToString());
        pluginValues.put(PluginTable.STATUS_SUPPORT, StatusSupport.HAS_SUPPORT_TRUE);
        ContentValues sourceValues = new ContentValues();
        sourceValues.put(SourceTable.NAME, mContext.getResources().getString(
                R.string.ts_snssample_service_name));
        sourceValues.put(SourceTable.ICON_URI, ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + mContext.getPackageName() + "/"
                + R.drawable.sina_icon);

        Cursor pluginCursor = null;
        try {
            // Register or update plugin table
            ContentResolver contentResolver = mContext.getContentResolver();

            pluginCursor = contentResolver.query(PLUGIN_PROVIDER_URI, null, null, null, null);
            if (pluginCursor != null && pluginCursor.moveToFirst()) {
                int configState = pluginCursor.getInt(pluginCursor
                        .getColumnIndex(PluginTable.CONFIGURATION_STATE));
                if (configState == ConfigState.NOT_CONFIGURED) {
                    pluginValues.put(PluginTable.CONFIGURATION_TEXT, mContext.getResources()
                            .getString(R.string.snssample_register_txt));
                } else {
                    String displayName = settings.getDisplayName();
                    pluginValues.put(PluginTable.CONFIGURATION_TEXT, mContext
                            .getString(R.string.ts_snssample_logout_label)
                            + " " + displayName);
                }
                contentResolver.update(PLUGIN_PROVIDER_URI, pluginValues, null, null);
            } else {
                pluginValues.put(PluginTable.CONFIGURATION_TEXT, mContext.getResources().getString(
                        R.string.ts_snssample_service_name));
                pluginValues.put(PluginTable.CONFIGURATION_STATE, ConfigState.NOT_CONFIGURED);

                contentResolver.insert(PLUGIN_PROVIDER_URI, pluginValues);
            }
            // Register or update source table
            Cursor sourceCursor = null;
            try {
                sourceCursor = contentResolver.query(SOURCE_PROVIDER_URI, null, null, null, null);
                if (sourceCursor != null && sourceCursor.moveToFirst()) {
                    // We should only have a single source
                    sourceId = sourceCursor.getInt(sourceCursor
                            .getColumnIndexOrThrow(SourceTable.ID_COLUMN));
                    contentResolver.update(SOURCE_PROVIDER_URI, sourceValues, SourceTable.ID_COLUMN
                            + " = ?", new String[] {
                        String.valueOf(sourceId)
                    });
                } else {
                    sourceValues.put(SourceTable.ENABLED, 1);
                    Uri uri = mContext.getContentResolver().insert(SOURCE_PROVIDER_URI,
                            sourceValues);
                    sourceId = ContentUris.parseId(uri);
                }
            } finally {
                if (sourceCursor != null) {
                    sourceCursor.close();
                }
            }
        } finally {
            if (pluginCursor != null) {
                pluginCursor.close();
            }
        }

        mSourceId = sourceId;
        return sourceId;
    }
    /**
     * Sets Own status in EventStream database.
     * @param message: Message to store.
     * @param timestamp: Timestamp of message.
     */
    public void setOwnStatus(String message, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(SourceTable.CURRENT_STATUS, message);
        values.put(SourceTable.STATUS_TIMESTAMP, timestamp);
        mContext.getContentResolver().update(SOURCE_PROVIDER_URI, values, null, null);
    }

    /**
     * 
     * @param databaseEventId
     * @return the last index from the delimiter.
     */
    public static String getEventId(String databaseEventId) {
        String result = databaseEventId;
        int index = databaseEventId.lastIndexOf(EVENT_ID_DELIMITER);

        if (index != -1) {
            result = databaseEventId.substring(0, index);
        }
        return result;
    }
/**
 * 
 * @param databaseEventId
 * @return the event type of the inserted event.
 */
    public static EventType getEventType(String databaseEventId) {
        String value = null;
        int index = databaseEventId.lastIndexOf(EVENT_ID_DELIMITER);
        EventType eventType = EventType.LINK_EVENT;

        if (index != -1) {
            value = databaseEventId.substring(index + 1);
        }
        if (value != null) {
            if (EVENT_TYPE_LINK_AS_STRING.equals(value)) {
                eventType = EventType.LINK_EVENT;
            } else if (EVENT_TYPE_STATUS_AS_STRING.equals(value)) {
                eventType = EventType.STATUS_EVENT;
            } else if (EVENT_TYPE_PHOTO_AS_STRING.equals(value)) {
                eventType = EventType.PHOTO_EVENT;
            } else if (EVENT_TYPE_MESSAGE_AS_STRING.equals(value)) {
                eventType = EventType.MESSAGE_EVENT;
            }
        }

        return eventType;
    }
    
    /***
     * @param eventId: The ID of the concerned event.
     * @param message: The message as a text string
     * @param title: The title of the event.
     * @param picture  URI to an image
     * @param friendId Friend ID of sender.
     * @param timestamp 
     * @param eventType Type of event
     * This function adds data regarding the concerned event in the EventStream.
     * They must be committed as well. 
     */
    public void addEvent(String eventId, String message, String title, String picture,
            String friendId, long timestamp, EventType eventType) {
        ContentValues eventValues = new ContentValues();
        String databaseEventId = generateEventId(eventId, eventType);
        Log.v("DATABASE","Adding EVENT: " +eventId + " " + message + " " + 
        		title + " " + picture + " " + friendId + eventType);

        eventValues.put(EventTable.EVENT_KEY, databaseEventId);
        eventValues.put(EventTable.SOURCE_ID, getSourceId());
        if (message != null) {
            eventValues.put(EventTable.MESSAGE, message);
        }
        if (picture != null) {
            eventValues.put(EventTable.IMAGE_URI, picture);
        }
        eventValues.put(EventTable.PUBLISHED_TIME, timestamp);
        if (title != null) {
            eventValues.put(EventTable.TITLE, title);
        }
        if (friendId != null) {
            eventValues.put(EventTable.FRIEND_KEY, friendId);
        }
        eventValues.put(EventTable.OUTGOING, 0);
        eventValues.put(EventTable.PERSONAL, 0);
        mContentValuesList.add(eventValues);
        Log.v("DATABASE", "Added event" + mContentValuesList.size());
    }
    
    /**
     * Events added to the EventStream are commited by this function  
     * @return number of events.
     */
    public int storeEvents() {
        int inserted = 0;
        Log.d("DATABASE", "Storing " + mContentValuesList.size() + " events.");
        if (mContentValuesList.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[mContentValuesList.size()];

            for (int i = 0; i < contentValuesArray.length; i++) {
                ContentValues contentValues = mContentValuesList.get(i);
                contentValuesArray[i] = contentValues;
            }

            inserted = mCr.bulkInsert(EVENT_PROVIDER_URI, contentValuesArray);
            Log.v(LOG_TAG, "Inserted " + inserted + "  statuses.");
            
            if (Config.DEBUG) {
                Log.v(LOG_TAG, "Inserted " + inserted + "  statuses.");
            }
        }
        mContentValuesList.clear();

        return inserted;
    }

    /**
     * @param friendId: Id of friend to add - ID from extended application.
     * @param profileImageUri: Uri to profile image
     * @param friendName: Name of the friend.
     * @param uri: Uri in contacts data base
     * 
     * This function adds a friend to the Eventstream Friend table.
     */
    public void addFriend(String friendId, String profileImageUri, String friendName,Uri uri) {
        ContentValues cv = new ContentValues();
        Set<String> myFriends = getRegisteredFriends();
        Log.v("DATABASE","AddingFriend");

        if (myFriends != null) {
            Log.v("DATABASE","AddingFriends " + myFriends.size()+ " friends found");

        	if (!myFriends.contains(friendId)) {
        		
                cv.put(FriendTable.SOURCE_ID, getSourceId());
                cv.put(FriendTable.PROFILE_IMAGE_URI, profileImageUri);
                cv.put(FriendTable.FRIEND_KEY, friendId);
                cv.put(FriendTable.DISPLAY_NAME, friendName);
                if (uri != null){
                	Log.v("DATABASE", "Contact uri:" + uri.toString());
                	cv.put(FriendTable.CONTACTS_REFERENCE, uri.toString());
                }
                mContentValuesList.add(cv);
                myFriends.add(friendId);
                Log.v("DATABASE","Added Friend" + friendId);

            } else {
                int updated = mCr.update(FRIEND_PROVIDER_URI, cv, FriendTable.FRIEND_KEY
                        + " = ? AND " + FriendTable.SOURCE_ID + " = ?", new String[] {
                        friendId, String.valueOf(getSourceId())
                });
                Log.v("DATABASE", "Updated " + updated + " friend(s) in Event Stream.");
                if (Config.DEBUG) {
                    Log.d(LOG_TAG, "Updated " + updated + " friend(s) in Event Stream.");
                }
            }
        }
        Log.v("DATABASE", "Added friend" + mContentValuesList.size());

    }
    /**
     * This function returns all registered friend Ids as a set of strings. 
     */
    public Set<String> getFriendIds() {
        return getRegisteredFriends();
    }

    /**
     * @param friendId: Th external friendId to check.
     * @return true if  friendId is already registered in the Friend 
     * table of the EventStream, otherwise false.
     * 
     */
    public boolean isFriend(String friendId) {
        Set<String> myFriends = getRegisteredFriends();
        if (myFriends != null) {
            return myFriends.contains(friendId);
        } else {
            return false;
        }
    }
    /**
     * @return the number of added friends that was stored in the EventStream.
     */
    public int storeFriends() {
        int inserted = 0;
        Log.d("DATABASE", "Storing " + mContentValuesList.size() + " friends.");

        if (mContentValuesList.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[mContentValuesList.size()];

            for (int i = 0; i < contentValuesArray.length; i++) {
                ContentValues contentValues = mContentValuesList.get(i);
                contentValuesArray[i] = contentValues;
            }
            inserted = mCr.bulkInsert(FRIEND_PROVIDER_URI, contentValuesArray);
            Log.d("DATABASE", "Inserted " + inserted + "  friends.");
            if (Config.DEBUG) {
                Log.d(LOG_TAG, "Inserted " + inserted + "  friends.");
            }
        }
        mContentValuesList.clear();

        return inserted;
    }

    /**
     * Remove a friend using the  ID
     *
     * @param friendId the  friend ID
     */
    public void removeFriend(String friendId) {
        // Delete the friend
        mCr.delete(FRIEND_PROVIDER_URI, FriendTable.FRIEND_KEY + "=?", new String[] {friendId});

        // Delete all events for the friend
        mCr.delete(EVENT_PROVIDER_URI, EventTable.FRIEND_KEY + "=" + friendId, null);

        // Delete in cache
        removeRegisterFriend(friendId);
    }



    /**
     * Set configuration text for the plugin (displayed in service list)
     *
     * @param configText the new configuration text
     * @return true if the update succeeded
     */
    public boolean setConfigurationText(String configText) {
        ContentValues values = new ContentValues();
        int updated = 0;

        try {
            values.put(PluginTable.CONFIGURATION_TEXT, configText);
            updated = mCr.update(PLUGIN_PROVIDER_URI, values, null, null);
        } catch (Exception exception) {
            // Do nothing, this is most likely caused by the fact that we are
            // not registerd
        }

        return (updated == 1);
    }
    /**
     * 
     * @param state Sets configuration state to this state. Please use state values as 
     * defined in interface EventStreamConstants.PluginTable.CONFIGURATION_STATE
     */
    public void setConfigurationState(int state) {
        ContentValues values = new ContentValues();

        try {
            values.put(EventStreamConstants.PluginTable.CONFIGURATION_STATE, state);
            mCr.update(EventStreamConstants.PLUGIN_PROVIDER_URI, values, null, null);
        } catch (Exception exception) {
            // Do nothing, this is most likely caused by the fact that we are
            // not registered
        }
    }

    /**
     * 
     * @param sourceId
     * @return the friend IDs associated  with the specified sourceId as set of strings.
     */
    private Set<String> getRegisteredFriendIds(long sourceId) {
        Set<String> registeredFriends = new HashSet<String>();
        Cursor friendsCursor = null;

        try {
            friendsCursor = mCr.query(FRIEND_PROVIDER_URI, null, FriendTable.SOURCE_ID + " = ?",
                    new String[] {
                        String.valueOf(sourceId)
                    }, null);
            while (friendsCursor != null && friendsCursor.moveToNext()) {
                registeredFriends.add(friendsCursor.getString(friendsCursor
                        .getColumnIndexOrThrow(FriendTable.FRIEND_KEY)));
            }
        } finally {
            if (friendsCursor != null) {
                friendsCursor.close();
            }
        }
        return registeredFriends;
    }

    /**
     * 
     * @return the sourceId used by this plugin.
     * 
     */
    private long getSourceId() {
        long result = mSourceId;
        if (result == -1) {
            Cursor c = null;
            try {
                c = mCr.query(SOURCE_PROVIDER_URI, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    result = c.getLong(c.getColumnIndex(SourceTable.ID_COLUMN));
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        return result;
    }
/**
 * 
 * @param eventId : External event ID
 * @param eventType: Event type as defined.
 * @return The Newly made Event id generated from Event id and event type.
 */
    private String generateEventId(String eventId, EventType eventType) {
        String eventTypeString = null;

        switch (eventType) {
            case LINK_EVENT:
                eventTypeString = EVENT_TYPE_LINK_AS_STRING;
                break;
            case STATUS_EVENT:
                eventTypeString = EVENT_TYPE_STATUS_AS_STRING;
                break;
            case PHOTO_EVENT:
                eventTypeString = EVENT_TYPE_PHOTO_AS_STRING;
                break;
            case MESSAGE_EVENT:
                eventTypeString = EVENT_TYPE_MESSAGE_AS_STRING;
                break;
            default:
                break;
        }
        if (eventTypeString == null) {
            return null;
        } else {
            return eventId + EVENT_ID_DELIMITER + eventTypeString;
        }
    }
/**
 * 
 * @param EventId
 * @return the friend key from the Event ID.
 */
    public static String extractFriendKeyFromEventId(String EventId) {
        String result = null;

        if (EventId != null) {
            int index = EventId.indexOf(FRIEND_ID_FACBOOK_OBJECT_ID_DELIMITER);

            if (index != -1) {
                result = EventId.substring(0, index);
            }
        }

        return result;
    }

    /**
     * 
     * @return the registered friendIds as a set of strings.
     */
    private Set<String> getRegisteredFriends() {
        if (mRegisteredFriends == null) {
            long sourceId = getSourceId();

            mRegisteredFriends = getRegisteredFriendIds(sourceId);
        }
        return mRegisteredFriends;
    }

    /**
     * 
     * @param id - ID of friend to remove.
     */
    private void removeRegisterFriend(String id) {
        if (mRegisteredFriends != null && id != null) {
            try {
                mRegisteredFriends.remove(id);
            } catch (Exception exception) {

            }
        }
    }

}
