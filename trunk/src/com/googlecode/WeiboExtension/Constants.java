package com.googlecode.WeiboExtension;

public interface Constants {

	public interface Config {
		public static final boolean DEBUG = true;
	}	
	
	public interface SinaConstants {
		public static final String CONSUMER_KEY = "2055101319";
		public static final String CONSUMER_SECRET = "6f5839e98b08344f5ff97f60881d64cd";
		
	    public static String SERVER = "https://api.weibo.com/2/";
	    public static String URL_OAUTH_TOKEN = "http://api.t.sina.com.cn/oauth/request_token";
	    public static String URL_ACCESS_TOKEN = "http://api.t.sina.com.cn/oauth/access_token";
	    public static String URL_AUTHORIZE = "http://api.t.sina.com.cn/oauth/authorize";
	    public static String URL_AUTHENTICATION = "http://api.t.sina.com.cn/oauth/authenticate";
	    
	    
	    public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
	}
	
	
    
    public static String CALLBACK_SCHEMA = "oauth";
    public static String CALLBACK_HOST = "callback";
    public static String CALLBACK_URL = CALLBACK_SCHEMA + "://" + CALLBACK_HOST;   
    
    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";
    
    
    
    /**
     * This file contains constant definition used throughout the sample extension project.
     */
        // Key used to authenticate intents sent from EventStream
        static final String PLUGIN_KEY_PARAMETER = "plugin_key";
        static final String PLUGIN_KEY = "PLUGIN_KEY_com.sonyericsson.eventstream.eventstreamplugin-673924817981782773912";

        static final String LOG_TAG = "SNSSampleExtension";

        // Shared preference constants, used in settings.
        static final String CONFIG_STORE = "SNSSample-plugin.conf";
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
