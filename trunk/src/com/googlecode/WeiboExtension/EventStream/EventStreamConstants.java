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

import android.net.Uri;

public interface EventStreamConstants {
    public abstract static class Config {
        public static final boolean DEBUG = true;
    }

    // Event Stream provider Uri:s
    public static final Uri FRIEND_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/friends");
    public static final Uri EVENT_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/events");
    public static final Uri SOURCE_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/sources");
    public static final Uri PLUGIN_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/plugins");
    public static final Uri EVENTS_FRIENDS_VIEW_URI = Uri.parse("content://com.sonyericsson.eventstream/events_friends");

    public static final String EVENTSTREAM_AUTHORITY = "com.sonyericsson.eventstream";

    // Sent from Event Stream to plugin
    public interface EventstreamIntents {
        public static final String REGISTER_PLUGINS_REQUEST_INTENT = "com.sonyericsson.eventstream.REGISTER_PLUGINS";
        public static final String REFRESH_REQUEST_INTENT = "com.sonyericsson.eventstream.REFRESH_REQUEST";
        public static final String VIEW_EVENT_INTENT = "com.sonyericsson.eventstream.VIEW_EVENT_DETAIL";
        public static final String STATUS_UPDATE_INTENT = "com.sonyericsson.eventstream.SEND_STATUS_UPDATE";
    }

    // Extra parameters used in intents
    public interface EventstreamIntentData {
        public static final String EXTRA_SOURCE_ID = "source_id";
        public static final String EXTRA_FRIEND_ID = "friend_id";
        public static final String EXTRA_EVENT_ID = "event_id";

        public static final String EXTRA_EVENT_KEY = "event_key";
        public static final String EXTRA_FRIEND_KEY = "friend_key";

        public static final String EXTRA_STATUS_UPDATE_MESSAGE = "new_status_message";
        public static final String EXTRA_FRIEND_USER_DATA = "friend_userdata";
        public static final String EXTRA_EVENT_USER_DATA = "event_userdata";
    }
    // Column definitions in the Event Stream provider
/**
 * Constants used in the Plugin Table
 */
    public interface PluginTable {
        public static final String API_VERSION = "api_version";
        public static final String CONFIGURATION_STATE = "config_state";
        public static final String CONFIGURATION_ACTIVITY = "config_activity";
        public static final String CONFIGURATION_TEXT = "config_text";
        public static final String NAME = "name";
        public static final String ICON_URI = "icon_uri";
        public static final String STATUS_SUPPORT = "status_support";
        public static final String STATUS_TEXT_MAX_LENGTH = "status_text_max_length";
        public static final String PLUGIN_KEY = "plugin_key";
    }
    /**
     * Constants used in the Source Table
     */

    public interface SourceTable {
        public static final String ID_COLUMN = "_id";
        public static final String PLUGIN_ID = "plugin_id";
        public static final String NAME = "name";
        public static final String ICON_URI = "icon_uri";
        public static final String ENABLED = "enabled";
        public static final String CURRENT_STATUS = "current_status";
        public static final String STATUS_TIMESTAMP = "status_timestamp";
    }

    /**
     * Constants used in the Event Table
     */
    public interface EventTable {
        public static final String SOURCE_ID = "source_id";
        public static final String PLUGIN_ID = "plugin_id";
        public static final String FRIEND_KEY = "friend_key";
        public static final String MESSAGE = "message";
        public static final String IMAGE_URI = "image_uri";
        public static final String PUBLISHED_TIME = "published_time";
        public static final String ICON1_URI = "icon1_uri";
        public static final String TITLE = "title";
        public static final String PERSONAL = "personal";
        public static final String OUTGOING = "outgoing";
        public static final String EVENT_KEY = "event_key";
    }

    /**
     * Constants used in the Friend Table
     */
public interface FriendTable {
        public static final String SOURCE_ID = "source_id";
        public static final String PLUGIN_ID = "plugin_id";
        public static final String DISPLAY_NAME = "display_name";
        public static final String PROFILE_IMAGE_URI = "profile_image_uri";
        public static final String CONTACTS_REFERENCE = "contacts_reference";
        public static final String FRIEND_KEY = "friend_key";
    }

    public interface ConfigState {
        public static final int CONFIGURED = 0;
        public static final int NOT_CONFIGURED = 1;
        public static final int CONFIGURATION_NOT_NEEDED = 2;
    }

    public interface StatusSupport {
        public static final int HAS_SUPPORT_FALSE = 0;
        public static final int HAS_SUPPORT_TRUE = 1;
    }
}
