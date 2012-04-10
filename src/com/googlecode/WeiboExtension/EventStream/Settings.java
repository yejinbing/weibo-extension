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

import static com.googlecode.WeiboExtension.EventStream.Constants.LAST_COMMUNICATION_WITH_SERVER_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.ACCESS_TOKEN_PREFS_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.ACCESS_TOKEN_SECRET_PREFS_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.CONFIG_STORE;
import static com.googlecode.WeiboExtension.EventStream.Constants.DISPLAY_NAME_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.HAS_ACCEPTED_DISCLAIMER;
import static com.googlecode.WeiboExtension.EventStream.Constants.LOCALE_HASH;
import static com.googlecode.WeiboExtension.EventStream.Constants.OWN_ID_PREFS_KEY;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings implements SNSsample.Settings {
    private static final int MODE_PRIVATE = android.content.Context.MODE_PRIVATE;
    private Context mContext = null;

    public Settings(Context context) {
        mContext = context;
    }

    /**
     * Remove all settings
     */
    public void removeSettings() {
        setToken(null);
        setTokenSecret(null);
        setLastCommunicationTime(null);
        setOwnId(0);
        setHasAcceptedDisclaimer(null);
        setDisplayName(null);
        setLocaleHash(null);
    }
    /**
     * 判断是否开启了自动提醒
     * @return
     */
    public boolean isAutomaticAlert() {
    	SharedPreferences prefs = mContext.getSharedPreferences("com.googlecode.WeiboExtension_preferences", 0);
    	return prefs.getBoolean("automatic_alert", false);
    }
    /**
     * 读取自动检测间隔
     * @return
     */
    public long getInspectionInterval() {
    	SharedPreferences prefs = mContext.getSharedPreferences("com.googlecode.WeiboExtension_preferences", 0);
    	String inspectionIntervalValue = prefs.getString("inspection_interval", null);
    	if (inspectionIntervalValue != null) {
    		return Long.parseLong(inspectionIntervalValue);
    	}else {
    		return 0;
    	}
    }
    public int getMaxRecord() {
    	int maxCache = 20;
		SharedPreferences prefs = mContext.getSharedPreferences("com.googlecode.WeiboExtension_preferences", 0);
		String maxCacheNum = prefs.getString("max_cache", null);
		if (maxCacheNum == null) {
			SharedPreferences.Editor prefsEditor = prefs.edit();
    		prefsEditor.putString("max_cache", maxCache + "");
    		prefsEditor.commit();
		}else {
			maxCache = Integer.parseInt(maxCacheNum);
		}
    	return maxCache;
    }
    
    public boolean isLoggedIn() {
        String accessToken = getToken();
        return (accessToken != null);
    }

    /**
     * Get the users own id.
     * Empty string ("") is returned if id is not set
     *
     * @return the id or "" if not set
     */
    public long getOwnId() {
        return getLong(Constants.OWN_ID_PREFS_KEY, 0);
    }

    /**
     * Set the own id
     * null will remove the setting
     *
     * @param id the new id
     */
    public void setOwnId(long id) {
        setLong(OWN_ID_PREFS_KEY, id);
    }

    /**
     * Get the display (real) name of the logged in user
     *
     * @return display name or "" if not set
     */
    public String getDisplayName() {
        return getString(DISPLAY_NAME_KEY, "");
    }

    /**
     * Set the display (real) name of the logged in user
     *
     * @param displayName display name
     */
    public void setDisplayName(String displayName) {
        if (displayName == null) {
            removeSetting(DISPLAY_NAME_KEY);
        } else {
            setString(DISPLAY_NAME_KEY, displayName);
        }
    }

    /**
     * Get the authentication token.
     * If not stored then null is returned
     *
     * @return the authentication token
     */
    public String getAuthenticationToken() {
        return getString(ACCESS_TOKEN_PREFS_KEY, null);
    }

    /**
     * Set the authentication token
     * null will remove the setting
     *
     * @param token the new token
     */
    public void setAuthenticationToken(final String token) {
        if (token == null) {
            removeSetting(ACCESS_TOKEN_PREFS_KEY);
        } else {
            setString(ACCESS_TOKEN_PREFS_KEY, token);
        }
    }

    /**
     * Get a hash code representation of the current locale
     *
     * @return the locale hash or null if not set
     */
    public Integer getLocaleHash() {
        return getInteger(LOCALE_HASH, 0);
    }

    /**
     * Set the current locale hash code
     *
     * @param locale current locale hash code
     */
    public void setLocaleHash(final Integer locale) {
        if (locale == null) {
            removeSetting(LOCALE_HASH);
        } else {
            setInteger(LOCALE_HASH, locale);
        }
    }


    /**
     * Set the last communication time
     * null will remove the setting
     *
     * @param time the new time
     */
    public void setLastCommunicationTime(Long time) {
        if (time == null) {
            removeSetting(LAST_COMMUNICATION_WITH_SERVER_KEY);
        } else {
            setLong(LAST_COMMUNICATION_WITH_SERVER_KEY, time);
        }
    }

    /**
     * Get the last communication time
     * Default value value is 0L
     *
     * @return the last time of communication
     */
   public Long getLastCommunicationTime() {
        return getLong(LAST_COMMUNICATION_WITH_SERVER_KEY, 0L);
    }


    public boolean hasAcceptedDisclaimer() {
        int value = getInteger(HAS_ACCEPTED_DISCLAIMER, 0);

        return (value != 0);
    }

    public void setHasAcceptedDisclaimer(Boolean value) {
        if (value == null) {
            removeSetting(HAS_ACCEPTED_DISCLAIMER);
        } else if (value) {
            setInteger(HAS_ACCEPTED_DISCLAIMER, 1);
        } else {
            setInteger(HAS_ACCEPTED_DISCLAIMER, 0);
        }
    }

    /**
     * Remove a setting
     *
     * @param key the setting to remove
     */
    private void removeSetting(String key) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);
        preferences.edit().remove(key).commit();
    }

    /**
     * Sets a new string setting
     *
     * @param key the key, null not allowed
     * @param value the new value, null not allowed
     */
    private void setString(String key, String value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putString(key, value).commit();
    }

    /**
     * Get a stored string setting
     *
     * @param key the key for the setting
     * @param defaultValue the default value if key not found
     * @return the setting
     */
    private String getString(String key, String defaultValue) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        return preferences.getString(key, defaultValue);
    }

    /**
     * Store a long setting
     *
     * @param key the key for the setting
     * @param value the value for the setting
     */
    private void setLong(String key, Long value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putLong(key, value).commit();
    }

    /**
     * Get a stored long setting
     *
     * @param key the key for the setting
     * @param defaultValue the default value if key not found
     * @return the setting
     */
    private Long getLong(String key, long defaultValue) {
        return mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE).getLong(key, defaultValue);
    }


    /**
     * Get a stored integer setting
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private Integer getInteger(String key, Integer defaultValue) {
        return mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE).getInt(key, defaultValue);
    }

    /**
     * Set an integer
     * @param key
     * @param value
     */
    private void setInteger(String key, int value) {
        SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_STORE, MODE_PRIVATE);

        preferences.edit().putInt(key, value).commit();
    }

	public String getToken() {
		// TODO Auto-generated method stub
		return getString(ACCESS_TOKEN_PREFS_KEY, null);
	}

	public void setToken(String token) {
		// TODO Auto-generated method stub
		if (token == null) {
            removeSetting(ACCESS_TOKEN_PREFS_KEY);
        } else {
            setString(ACCESS_TOKEN_PREFS_KEY, token);
        }
	}

	public String getTokenSecret() {
		// TODO Auto-generated method stub
		return getString(ACCESS_TOKEN_SECRET_PREFS_KEY, null);
	}

	public void setTokenSecret(String tokenSecret) {
		// TODO Auto-generated method stub
		if (tokenSecret == null) {
            removeSetting(ACCESS_TOKEN_SECRET_PREFS_KEY);
        } else {
            setString(ACCESS_TOKEN_SECRET_PREFS_KEY, tokenSecret);
        }
	}
}
