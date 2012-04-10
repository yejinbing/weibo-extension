/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sinaweibo4android;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import sinaweibo4android.api.Account;
import sinaweibo4android.api.Comment;
import sinaweibo4android.api.Emotion;
import sinaweibo4android.api.Favorites;
import sinaweibo4android.api.Status;
import sinaweibo4android.api.User;
import sinaweibo4android.api.UserWapper;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieSyncManager;

/**
 * Encapsulation main Weibo APIs, Include: 1. getRquestToken , 2.
 * getAccessToken, 3. url request. Used as a single instance class. Implements a
 * weibo api as a synchronized way.
 * 
 * @author ZhangJie (zhangjie2@staff.sina.com.cn)
 */
public class Weibo {

    // public static String SERVER = "http://api.t.sina.com.cn/";
    public static String SERVER = "https://api.weibo.com/2/";
    public static String URL_OAUTH_TOKEN = "http://api.t.sina.com.cn/oauth/request_token";
    public static String URL_AUTHORIZE = "http://api.t.sina.com.cn/oauth/authorize";
    public static String URL_ACCESS_TOKEN = "http://api.t.sina.com.cn/oauth/access_token";
    public static String URL_AUTHENTICATION = "http://api.t.sina.com.cn/oauth/authenticate";

    public static String URL_OAUTH2_ACCESS_TOKEN = "https://api.weibo.com/oauth2/access_token";

    // public static String URL_OAUTH2_ACCESS_AUTHORIZE =
    // "http://t.weibo.com:8093/oauth2/authorize";
    public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

    private static String APP_KEY = "";
    private static String APP_SECRET = "";

    private static Weibo mWeiboInstance = null;
    private Token mAccessToken = null;
    private RequestToken mRequestToken = null;

    private WeiboDialogListener mAuthDialogListener;

    private static final int DEFAULT_AUTH_ACTIVITY_CODE = 32973;

    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";
    public static final String DEFAULT_REDIRECT_URI = "wbconnect://success";// 暂不支持
    public static final String DEFAULT_CANCEL_URI = "wbconnect://cancel";// 暂不支持

    private String mRedirectUrl;

    private Weibo() {
        Utility.setRequestHeader("Accept-Encoding", "gzip");
        Utility.setTokenObject(this.mRequestToken);
        mRedirectUrl = DEFAULT_REDIRECT_URI;
    }

    public synchronized static Weibo getInstance() {
        if (mWeiboInstance == null) {
            mWeiboInstance = new Weibo();
        }
        return mWeiboInstance;
    }

    // 设置accessToken
    public void setAccessToken(AccessToken token) {
        mAccessToken = token;
    }

    public Token getAccessToken() {
        return this.mAccessToken;
    }

    public void setupConsumerConfig(String consumer_key, String consumer_secret) {
        Weibo.APP_KEY = consumer_key;
        Weibo.APP_SECRET = consumer_secret;
    }

    public static String getAppKey() {
        return Weibo.APP_KEY;
    }

    public static String getAppSecret() {
        return Weibo.APP_SECRET;
    }

    public void setRequestToken(RequestToken token) {
        this.mRequestToken = token;
    }

    public static String getSERVER() {
        return SERVER;
    }

    public static void setSERVER(String sERVER) {
        SERVER = sERVER;
    }

    // 设置oauth_verifier
    public void addOauthverifier(String verifier) {
        mRequestToken.setVerifier(verifier);
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public void setRedirectUrl(String mRedirectUrl) {
        this.mRedirectUrl = mRedirectUrl;
    }

    /**
     * Requst sina weibo open api by get or post
     * 
     * @param url
     *            Openapi request URL.
     * @param params
     *            http get or post parameters . e.g.
     *            gettimeling?max=max_id&min=min_id max and max_id is a pair of
     *            key and value for params, also the min and min_id
     * @param httpMethod
     *            http verb: e.g. "GET", "POST", "DELETE"
     * @throws IOException
     * @throws MalformedURLException
     * @throws WeiboException
     */
    public String request(Context context, String url, WeiboParameters params, String httpMethod,
            Token token) throws WeiboException {
        String rlt = Utility.openUrl(context, url, httpMethod, params, this.mAccessToken);
        return rlt;
    }

    /**/
    public RequestToken getRequestToken(Context context, String key, String secret,
            String callback_url) throws WeiboException {
        Utility.setAuthorization(new RequestTokenHeader());
        WeiboParameters postParams = new WeiboParameters();
        postParams.add("oauth_callback", callback_url);
        String rlt;
        rlt = Utility.openUrl(context, Weibo.URL_OAUTH_TOKEN, "POST", postParams, null);
        RequestToken request = new RequestToken(rlt);
        this.mRequestToken = request;
        return request;
    }

    public AccessToken generateAccessToken(Context context, RequestToken requestToken)
            throws WeiboException {
        Utility.setAuthorization(new AccessTokenHeader());
        WeiboParameters authParam = new WeiboParameters();
        authParam.add("oauth_verifier", this.mRequestToken.getVerifier()/* "605835" */);
        authParam.add("source", APP_KEY);
        String rlt = Utility.openUrl(context, Weibo.URL_ACCESS_TOKEN, "POST", authParam,
                this.mRequestToken);
        AccessToken accessToken = new AccessToken(rlt);
        this.mAccessToken = accessToken;
        return accessToken;
    }

    public AccessToken getXauthAccessToken(Context context, String app_key, String app_secret,
            String usrname, String password) throws WeiboException {
        Utility.setAuthorization(new XAuthHeader());
        WeiboParameters postParams = new WeiboParameters();
        postParams.add("x_auth_username", usrname);
        postParams.add("x_auth_password", password);
        postParams.add("oauth_consumer_key", APP_KEY);
        String rlt = Utility.openUrl(context, Weibo.URL_ACCESS_TOKEN, "POST", postParams, null);
        AccessToken accessToken = new AccessToken(rlt);
        this.mAccessToken = accessToken;
        return accessToken;
    }

    /**
     * 获取Oauth2.0的accesstoken
     * 
     * https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&
     * client_secret=YOUR_CLIENT_SECRET&grant_type=password&redirect_uri=
     * YOUR_REGISTERED_REDIRECT_URI&username=USER_NAME&pasword=PASSWORD
     * 
     * @param context
     * @param app_key
     * @param app_secret
     * @param usrname
     * @param password
     * @return
     * @throws WeiboException
     */
    public Oauth2AccessToken getOauth2AccessToken(Context context, String app_key,
            String app_secret, String usrname, String password) throws WeiboException {
        Utility.setAuthorization(new Oauth2AccessTokenHeader());
        WeiboParameters postParams = new WeiboParameters();
        postParams.add("username", usrname);
        postParams.add("password", password);
        postParams.add("client_id", app_key);
        postParams.add("client_secret", app_secret);
        postParams.add("grant_type", "password");
        String rlt = Utility.openUrl(context, Weibo.URL_OAUTH2_ACCESS_TOKEN, "POST", postParams,
                null);
        Oauth2AccessToken accessToken = new Oauth2AccessToken(rlt);
        this.mAccessToken = accessToken;
        return accessToken;
    }

    private boolean startSingleSignOn(Activity activity, String applicationId,
            String[] permissions, int activityCode) {
        return false;
    }

    private void startDialogAuth(Activity activity, String[] permissions) {
        WeiboParameters params = new WeiboParameters();
        if (permissions.length > 0) {
            params.add("scope", TextUtils.join(",", permissions));
        }
        CookieSyncManager.createInstance(activity);
        dialog(activity, params, new WeiboDialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                if (null == mAccessToken) {
                    mAccessToken = new Token();
                }
                mAccessToken.setToken(values.getString(TOKEN));
                mAccessToken.setExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Log.d("Weibo-authorize",
                            "Login Success! access_token=" + mAccessToken.getToken() + " expires="
                                    + mAccessToken.getExpiresIn());
                    mAuthDialogListener.onComplete(values);
                } else {
                    Log.d("Weibo-authorize", "Failed to receive access token");
                    mAuthDialogListener.onWeiboException(new WeiboException(
                            "Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onWeiboException(WeiboException error) {
                Log.d("Weibo-authorize", "Login failed: " + error);
                mAuthDialogListener.onWeiboException(error);
            }

            public void onCancel() {
                Log.d("Weibo-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }

    /**
     * User-Agent Flow
     * 
     * @param activity
     * 
     * @param listener
     *            授权结果监听器
     */
    public void authorize(Activity activity, final WeiboDialogListener listener) {
        authorize(activity, new String[] {}, DEFAULT_AUTH_ACTIVITY_CODE, listener);
    }

    private void authorize(Activity activity, String[] permissions,
            final WeiboDialogListener listener) {
        authorize(activity, permissions, DEFAULT_AUTH_ACTIVITY_CODE, listener);
    }

    private void authorize(Activity activity, String[] permissions, int activityCode,
            final WeiboDialogListener listener) {
        Utility.setAuthorization(new Oauth2AccessTokenHeader());

        boolean singleSignOnStarted = false;
        mAuthDialogListener = listener;

        // Prefer single sign-on, where available.
        if (activityCode >= 0) {
            singleSignOnStarted = startSingleSignOn(activity, APP_KEY, permissions, activityCode);
        }
        // Otherwise fall back to traditional dialog.
        if (!singleSignOnStarted) {
            startDialogAuth(activity, permissions);
        }

    }

    private void authorizeCallBack(int requestCode, int resultCode, Intent data) {

    }

    public void dialog(Context context, WeiboParameters parameters,
            final WeiboDialogListener listener) {
        parameters.add("client_id", APP_KEY);
        parameters.add("response_type", "token");
        parameters.add("redirect_uri", mRedirectUrl);
        parameters.add("display", "mobile");

        if (isSessionValid()) {
            parameters.add(TOKEN, mAccessToken.getToken());
        }
        String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Utility.showAlert(context, "Error",
                    "Application requires permission to access the Internet");
        } else {
            new WeiboDialog(this, context, url, listener).show();
        }
    }

    public boolean isSessionValid() {
        if (mAccessToken != null) {
            return (!TextUtils.isEmpty(mAccessToken.getToken()) && (mAccessToken.getExpiresIn() == 0 || (System
                    .currentTimeMillis() < mAccessToken.getExpiresIn())));
        }
        return false;
    }
    
    
    
    /**************************************************************************************
     * 
     * 以下部分为新浪微博V2的api 
     * @author yejb 2012.3.13
     * 
     *************************************************************************************/
    
	//----------------------------账号 读取接口-------------------------------------------
    /**
	 * OAuth授权之后，获取授权用户的UID
	 * @param context
	 * @return Account
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/account/get_uid">account/get_uid </a>
	 */
    public Account getAccount(Context context) throws WeiboException {
    	WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        String rlt = request(context, 
				SERVER + "account/get_uid.json",
				bundle, "GET", mAccessToken);
		return Account.constructAccount(rlt);
    }
    
	//----------------------------微博 读取接口-------------------------------------------
    /**
	 * 根据ID获取单条微博消息，以及该微博消息的作者信息。
	 * @param id 要获取的微博消息ID
	 * @return 微博消息
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/show">statuses/show </a>
	 */
    public Status showStatus(Context context, long id) throws WeiboException {
    	WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("id", id + "");
        String rlt = request(context, 
				SERVER + "statuses/show.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatus(rlt);
    }
    /**
	 * 返回最新的20条公共微博。返回结果非完全实时，最长会缓存60秒
	 * @return list of statuses of the Public Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
	 */
	public List<Status> getPublicTimeline(Context context) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "statuses/public_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}	
	/**
	 * 返回最新的20条公共微博。返回结果非完全实时，最长会缓存60秒
	 * @param context
	 * @param sinceId 返回ID比since_id大的微博（即比since_id时间晚的微博）
	 * @return list of statuses of the Public Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
	 */
	public List<Status> getPublicTimeline(Context context, long sinceId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("since_id", sinceId + "");
        String rlt = request(context, 
				SERVER + "statuses/public_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取当前登录用户及其所关注用户的最新20条微博消息。<br/>
	 * 和用户登录 http://t.sina.com.cn 后在“我的首页”中看到的内容相同。
	 * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
	 *
	 * @return list of the Friends Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/friends_timeline"> statuses/friends_timeline </a>
	 */
	public List<Status> getFriendsTimeline(Context context) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "statuses/friends_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}	
	/**
	 * 返回最新的20条公共微博。返回结果非完全实时，最长会缓存60秒
	 * @param context
	 * @param sinceId 返回ID比since_id大的微博（即比since_id时间晚的微博）
	 * @return list of statuses of the Public Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.t.sina.com.cn/wiki/index.php/Statuses/public_timeline">statuses/public_timeline </a>
	 */
	public List<Status> getFriendsTimeline(Context context, long sinceId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("since_id", sinceId + "");
        String rlt = request(context, 
				SERVER + "statuses/friends_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取当前登录用户及其所关注用户的最新微博消息。<br/>
	 * 和用户登录 http://t.sina.com.cn 后在“我的首页”中看到的内容相同。
	 * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
	 * @param context 
	 * @param count 单页返回的记录条数
	 * @return list of the Friends Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/friends_timeline"> statuses/friends_timeline </a>
	 */
	public List<Status> getFriendsTimeline(Context context, int count) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", count + "");
        String rlt = request(context, 
				SERVER + "statuses/friends_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取当前登录用户及其所关注用户的最新微博消息。<br/>
	 * 和用户登录 http://t.sina.com.cn 后在“我的首页”中看到的内容相同。
	 * <br>This method calls http://api.t.sina.com.cn/statuses/friends_timeline.format
	 * @param context 
	 * @param count 单页返回的记录条数
	 * @return list of the Friends Timeline
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/friends_timeline"> statuses/friends_timeline </a>
	 */
	public List<Status> getFriendsTimeline(Context context, 
			int count, long sinceId, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", count + "");
        if (sinceId > 0) {
        	bundle.add("since_id", sinceId + "");
        }
        if (maxId > 0 && maxId > sinceId) {
        	bundle.add("max_id", maxId + "");
        }
        String rlt = request(context, 
				SERVER + "statuses/friends_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取某个用户较老的微博列表
	 * @param context
	 * @param userId 需要查询的用户ID
	 * @param maxId 返回ID小于或等于max_id的微博
	 * @return the 20 most recent statuses posted in the last 24 hours from the user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/user_timeline">statuses/user_timeline</a>
	 */
	public List<Status> getUserTimelineBefore(Context context, 
			long userId, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("max_id", maxId + "");
        String rlt = request(context, 
				SERVER + "statuses/user_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取某个用户最新发表的微博列表
	 * @param context
	 * @param userId 需要查询的用户ID
	 * @return the 20 most recent statuses posted in the last 24 hours from the user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/user_timeline">statuses/user_timeline</a>
	 */
	public List<Status> getUserTimeline(Context context, long userId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "statuses/user_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取某个用户最新发表的微博列表
	 * @param context
	 * @param userId 需要查询的用户ID
	 * @param sinceId 返回ID比since_id大的微博（即比since_id时间晚的微博）
	 * @return the 20 most recent statuses posted in the last 24 hours from the user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/user_timeline">statuses/user_timeline</a>
	 */
	public List<Status> getUserTimeline(Context context, 
			long userId, long sinceId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("since_id", sinceId + "");
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "statuses/user_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取某个用户最新发表的微博列表
	 * @param context
	 * @param userId 需要查询的用户ID
	 * @param count 单页返回的记录条数
	 * @return the 20 most recent statuses posted in the last 24 hours from the user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/user_timeline">statuses/user_timeline</a>
	 */
	public List<Status> getUserTimeline(Context context, 
			long userId, int count) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", count + "");
        String rlt = request(context, 
				SERVER + "statuses/user_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 获取某个用户发表的微博列表
	 * @param context
	 * @param userId 需要查询的用户ID
	 * @param count 单页返回的记录条数
	 * @param sincId 返回ID比since_id大的微博（即比since_id时间晚的微博）
	 * @param maxId 返回ID小于或等于max_id的微博
	 * @return the 20 most recent statuses posted in the last 24 hours from the user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/user_timeline">statuses/user_timeline</a>
	 */
	public List<Status> getUserTimeline(Context context, 
			long userId, int count, long sincId, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", count + "");
        if (sincId > 0) {
        	bundle.add("since_id", sincId + "");
        }
        if (maxId > 0 && maxId > sincId) {
        	bundle.add("max_id", maxId + "");
        }
        String rlt = request(context, 
				SERVER + "statuses/user_timeline.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 返回最新20条提到登录用户的微博消息（即包含@username的微博消息）
	 * @param context
	 * @return the 20 most recent replies
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/mentions">statuses/mentions </a>
	 */
	public List<Status> getMentions(Context context) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "statuses/mentions.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 返回最新20条提到登录用户的微博消息（即包含@username的微博消息）
	 * @param context
	 * @return the 20 most recent replies
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/mentions">statuses/mentions </a>
	 */
	public List<Status> getMentions(Context context, long sinceId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("since_id", sinceId + "");
        String rlt = request(context, 
				SERVER + "statuses/mentions.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	/**
	 * 返回最新20条提到登录用户的微博消息（即包含@username的微博消息）
	 * @param context
	 * @return the 20 most recent replies
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/mentions">statuses/mentions </a>
	 */
	public List<Status> getMentionsBefore(Context context, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("max_id", maxId + "");
        String rlt = request(context, 
				SERVER + "statuses/mentions.json",
				bundle, "GET", mAccessToken);
		return Status.constructStatuses(rlt);
	}
	//----------------------------微博 写入接口-------------------------------------------
	/**
	 * 发布一条新微博
	 * @param context
	 * @param status 要发布的微博文本内容，内容不超过140个汉字
	 * @return the 20 most recent replies
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/update">statuses/update </a>
	 */
	public Status update(Context context, String status) throws WeiboException  {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        if (!TextUtils.isEmpty(status)) {
        	bundle.add("status", status);
        }
        String rlt = request(context, 
				SERVER + "statuses/update.json",
				bundle, "POST", mAccessToken);
		return Status.constructStatus(rlt);
	}
	/**
	 * 上传图片并发布一条新微博
	 * @param context
	 * @param status 要发布的微博文本内容，内容不超过140个汉字
	 * @param picPath 图片路径
	 * @return the 20 most recent replies
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/statuses/upload">statuses/upload </a>
	 */
	public Status upload(Context context, String status, String picPath) throws WeiboException  {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        if (!TextUtils.isEmpty(status)) {
        	bundle.add("status", status);
        }
        if (!TextUtils.isEmpty(picPath)) {
        	bundle.add("pic", picPath);
        }
        String rlt = request(context, 
				SERVER + "statuses/upload.json",
				bundle, "POST", mAccessToken);
		return Status.constructStatus(rlt);
	}
	/**
	 * 获取微博官方表情的详细信息
	 * @param context
	 * @param type 表情类别，face：普通表情、ani：魔法表情、cartoon：动漫表情，默认为face
	 * @return List<Emotions>
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/emotions">emotions </a>
	 */
	public List<Emotion> getEmotions(Context context, String type) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("access_token", mAccessToken.getToken());
		if (!TextUtils.isEmpty(type)) {
			bundle.add("type", type);
		}
		String rlt = request(context, 
				SERVER + "emotions.json", 
				bundle, "GET", mAccessToken);
		return Emotion.constructEmotions(rlt);
	}
	//----------------------------用户接口-------------------------------------------
	/**
	 * 按用户ID或昵称返回用户资料以及用户的最新发布的一条微博消息。
	 * @param context
	 * @param userId 用户ID或者昵称(long)
	 * @return User
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/users/show">users/show </a>
	 * @since Weibo4J 1.2.1
	 */
	public User showUser(Context context, long userId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("uid", userId + "");
        bundle.add("access_token", mAccessToken.getToken());
        String rlt = request(context, 
				SERVER + "users/show.json",
				bundle, "GET", mAccessToken);
        return User.constructUser(rlt);
	}
	//----------------------------收藏接口-------------------------------------------
	/**
	 * 收藏一条微博消息
	 * @param id the ID of the status to favorite
	 * @return Status
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/favorites/create">favorites/create </a>
	 */
	public Favorites createFavorite(Context context, long id) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("id", id + "");
        String rlt = request(context, 
				SERVER + "favorites/create.json",
				bundle, "POST", mAccessToken);
        return Favorites.constructFavorites(rlt);
	}
	/**
	 * 删除微博收藏.注意：只能删除自己收藏的信息
	 * @param id the ID of the status to un-favorite
	 * @return Status
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/favorites/destroy">favorites/destroy </a>
	 */
	public Favorites destroyFavorite(Context context, long id) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("id", id + "");
        String rlt = request(context, 
				SERVER + "favorites/destroy.json",
				bundle, "POST", mAccessToken);
        return Favorites.constructFavorites(rlt);
	}
	//----------------------------关系  关注读取接口-------------------------------------------
	/**
	 * 获取最多20条当前用户关注列表及每个关注用户的最新一条微博，返回结果按关注时间倒序排列，最新关注的用户排在最前面。
	 * @param context
	 * @param userId 需要查询的用户UID
	 * @return the list of UserWapper
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/friends">friendships/friends </a>
	 */
	public List<User> getFriends(Context context, long userId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", userId + "");
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "friendships/friends.json",
				bundle, "GET", mAccessToken);
        return User.constructUsers(rlt);
	}
	/**
	 * 获取用户关注列表及每个关注用户的最新一条微博，返回结果按关注时间倒序排列，最新关注的用户排在最前面。
	 * @param context
	 * @param userId 需要查询的用户UID
	 * @param count 单页返回的记录条数
	 * @return the list of UserWapper
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/friendships/friends </a>
	 */
	public List<User> getFriends(Context context,
			long userId, int count) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", userId + "");
        bundle.add("count", count + "");
        String rlt = request(context, 
				SERVER + "friendships/friends.json",
				bundle, "GET", mAccessToken);
        return User.constructUsers(rlt);
	}
	/**
	 * 获取用户关注列表及每个关注用户的最新一条微博，返回结果按关注时间倒序排列，最新关注的用户排在最前面。
	 * @param context
	 * @param userId 需要查询的用户UID
	 * @param cursor 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return the list of UserWapper
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/friends">friendships/friends </a>
	 */
	public UserWapper getFriendsWapper(Context context,
			long userId, int cursor) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", userId + "");
        bundle.add("count", 20 + "");
        bundle.add("cursor", cursor + "");
        String rlt = request(context, 
				SERVER + "friendships/friends.json",
				bundle, "GET", mAccessToken);
        return UserWapper.constructUserWapper(rlt);
	}
	//----------------------------关系  粉丝读取接口-------------------------------------------
	/**
	 * 获取用户粉丝列表及每个粉丝用户的最新一条微博，返回结果按关注时间倒序排列，最新关注的用户排在最前面。
	 * @param context
	 * @param userId 需要查询的用户UID
	 * @param cursor 返回结果的游标，下一页用返回值里的next_cursor，上一页用previous_cursor，默认为0
	 * @return the list of UserWapper
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/followers">friendships/followers </a>
	 */
	public UserWapper getFollowersWapper(Context context,
			long userId, int cursor) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", userId + "");
        bundle.add("count", 20 + "");
        bundle.add("cursor", cursor + "");
        String rlt = request(context, 
				SERVER + "friendships/followers.json",
				bundle, "GET", mAccessToken);
        return UserWapper.constructUserWapper(rlt);
	}
	//----------------------------关系  写入接口-------------------------------------------
	/**
	 * 关注一个用户。关注成功则返回关注人的资料
	 * @param id 要关注的用户ID 或者微博昵称 
	 * @return the befriended user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/create">friendships/create </a>
	 */
	public User createFriendship(Context context, long id) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", id + "");
        String rlt = request(context, 
				SERVER + "friendships/create.json",
				bundle, "POST", mAccessToken);
        return User.constructUser(rlt);
	}
	/**
	 * 取消关注一个用户。关注成功则返回关注人的资料
	 * @param id 要关注的用户ID 或者微博昵称 
	 * @return the befriended user
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/friendships/create">friendships/create </a>
	 */
	public User destroyFriendship(Context context, long id) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("uid", id + "");
        String rlt = request(context, 
				SERVER + "friendships/destroy.json",
				bundle, "POST", mAccessToken);
        return User.constructUser(rlt);
	}
	
	//----------------------------评论读取接口-------------------------------------------
	/**
	 * 根据微博ID返回某条微博的评论列表20条评论
	 * @param context
	 * @param id 需要查询的微博ID
	 * @return list of comments
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/comments/show">statuses/public_timeline </a>
	 */
	public List<Comment> getComments(Context context, long id) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("id", id + "");
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "comments/show.json",
				bundle, "GET", mAccessToken);
        return Comment.constructComments(rlt);
	}
	/**
	 * 根据微博ID返回某条微博的评论列表20条评论
	 * @param context
	 * @param id 需要查询的微博ID
	 * @param maxId 返回ID小于或等于max_id的评论
	 * @return list of comments
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/comments/show">statuses/public_timeline </a>
	 */
	public List<Comment> getComments(Context context, long id, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("id", id + "");
        bundle.add("count", 20 + "");
        bundle.add("max_id", maxId + "");
        String rlt = request(context, 
				SERVER + "comments/show.json",
				bundle, "GET", mAccessToken);
        return Comment.constructComments(rlt);
	}
	/**
	 * 根据微博ID返回某条微博的最新评论列表20条评论
	 * @param context
	 * @param id 需要查询的微博ID
	 * @return list of comments
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/comments/to_me">comments/to_me </a>
	 */
	public List<Comment> getCommentsToMe(Context context) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        String rlt = request(context, 
				SERVER + "comments/to_me.json",
				bundle, "GET", mAccessToken);
        return Comment.constructComments(rlt);
	}
	/**
	 * 根据微博ID返回某条微博的评论列表20条评论
	 * @param context
	 * @param id 需要查询的微博ID
	 * @param sinceId 返回ID大于sinceId的评论
	 * @return list of comments
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/comments/to_me">comments/to_me </a>
	 */
	public List<Comment> getCommentsToMe(Context context, long sinceId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("since_id", sinceId + "");
        String rlt = request(context, 
				SERVER + "comments/to_me.json",
				bundle, "GET", mAccessToken);
        return Comment.constructComments(rlt);
	}
	/**
	 * 根据微博ID返回某条微博的评论列表20条评论
	 * @param context
	 * @param id 需要查询的微博ID
	 * @param sinceId 返回ID大于sinceId的评论
	 * @return list of comments
	 * @throws WeiboException when Weibo service or network is unavailable
	 * @see <a href="http://open.weibo.com/wiki/2/comments/to_me">comments/to_me </a>
	 */
	public List<Comment> getCommentsToMeBefore(Context context, long maxId) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
        bundle.add("access_token", mAccessToken.getToken());
        bundle.add("count", 20 + "");
        bundle.add("max_id", maxId + "");
        String rlt = request(context, 
				SERVER + "comments/to_me.json",
				bundle, "GET", mAccessToken);
        return Comment.constructComments(rlt);
	}
	
}
