package com.googlecode.WeiboExtension;


/** 
 * 类说明：   OAuth认证返回的数据集合
 * @author  @Cundong
 * @weibo   http://weibo.com/liucundong
 * @blog    http://www.liucundong.com
 * @date    Apr 29, 2011 2:50:48 PM
 * @version 1.0
 */
public class AccessInfo 
{
	//用户Id
	private long userId;
	
	//accessToken
	private String accessToken;
	
	//consumerKey
	private String consumerKey;
	
	//screenName
	private String screenName;
	
	//标识是那种微博
	private int weiboMark;
	
	
	
	private static AccessInfo mAccessInfoInstance = null;
	
	public synchronized static AccessInfo getInstance() {
		if (mAccessInfoInstance == null) {
			mAccessInfoInstance = new AccessInfo();
		}
		return mAccessInfoInstance;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getScreenName() {
		return screenName;
	}
	
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	public int getWeiboMark() {
		return weiboMark;
	}
	
	public void setWeiboMark(int mark) {
		this.weiboMark = mark;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
	public String getConsumerKey() {
		return consumerKey;
	}
}