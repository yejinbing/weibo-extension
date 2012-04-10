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

public interface Constants {
/**
 * This file contains constant definition used throughout the sample extension project.
 */
    // Key used to authenticate intents sent from EventStream
    static final String PLUGIN_KEY_PARAMETER = "plugin_key";
    static final String PLUGIN_KEY = "PLUGIN_KEY_com.sonyericsson.eventstream.eventstreamplugin-673924817981782773912";

    static final String LOG_TAG = "SNSSampleExtension";

    // Shared preference constants, used in settings.
    static final String CONFIG_STORE = "SNSSample-plugin.conf";
    static final String ACCESS_TOKEN_SECRET_PREFS_KEY = "SNSSample.token_secret";
    static final String ACCESS_TOKEN_PREFS_KEY = "SNSSample.access_token";
    static final String LAST_COMMUNICATION_WITH_SERVER_KEY = "SNSServer.communication.lasttime";
    static final String OWN_ID_PREFS_KEY = "SNSSample.own.id";
    static final String HAS_ACCEPTED_DISCLAIMER = "SNSSample.accepted_disclaimer";
    static final String LOCALE_HASH = "SNSSample.locale.hash";
    static final String DISPLAY_NAME_KEY = "SNSSample.display.name";

    static final int STATUS_TEXT_MAX_LENGTH = 420;

    // Intent constants used internally within the extension when throwng internal events.
    static final String SEND_STATUS_UPDATE_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.SEND_STATUS_UPDATE";
    static final String REGISTER_PLUGIN_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.REGISTER_PLUGIN";
    static final String REFRESH_REQUEST_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.REFRESH_REQUEST";
    static final String AUTHENTICATE_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.AUTHENTICATE";
    static final String LOGOUT_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.LOGOUT";
    static final String LAUNCH_BROWSER_INTENT = "com.sonyericsson.eventstream.samplesnsplugin.VIEW_TILE";

    static final String USERNAME_EXTRA = "username";
    static final String PASSWORD_EXTRA = "password";
    static final String FRIEND_ID_EXTRA = "friend_id";
    static final String SERVICE_ID_EXTRA = "service_id";

    // For LAUNCH_BROWSER_INTENT
    static final String EVENT_KEY_EXTRA = "event_key";
    static final String FRIEND_KEY_EXTRA = "friend_key";

}
