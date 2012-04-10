/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sinaweibo4android.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sinaweibo4android.WeiboException;
import sinaweibo4android.json.JSONUtility;

/**
 * A data class representing Basic user information element
 */
public class User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3473349966713163765L;
	static final String[] POSSIBLE_ROOT_NAMES = new String[]{"user", "sender", "recipient", "retweeting_user"};
	
	private long id;                      	//用户id
	private String screenName;            	//微博昵称
	private String name;                  	//友好显示名称，如Bill Gates(此特性暂不支持)
	private int province;                 	//省份编码（参考省份编码表）
	private int city;                     	//城市编码（参考城市编码表）
	private String location;              	//地址
	private String description;           	//个人描述
	private String url;                   	//用户博客地址
	private String profileImageUrl;       	//自定义图像
	private String userDomain;            	//用户个性化URL
	private String gender;                	//性别,m--男，f--女,n--未知
	private int followersCount;           	//粉丝数
	private int friendsCount;             	//关注数
	private int statusesCount;            	//微博数
	private int favouritesCount;          	//收藏数
	private Date createdAt;               	//创建时间
	private boolean following;            	//保留字段,是否已关注(此特性暂不支持)
	private boolean allowAllActMsg;		  	//是否允许所有人给我发私信
	private boolean geoEnabled;           	//地理		
	private boolean verified;             	//加V标示，是否微博认证用户
	private boolean allowAllComment;	  	//是否允许所有人对我的微博进行评论
	private String avatarLarge;				//用户大头像地址
	private String verifiedReason;			//认证原因
	private boolean followMe;				//该用户是否关注当前登录用户
	private int onlineStatus;				//用户的在线状态，0：不在线、1：在线
	private int biFollowersCount;			//用户的互粉数	
	private Status status = null;

	public User(JSONObject json) throws WeiboException {
		if(json!=null){
			try {
				id = json.getLong("id");
				screenName = json.getString("screen_name");
				name = json.getString("name");
				province = JSONUtility.getInt("province", json);
				city = JSONUtility.getInt("city", json);
				location = json.getString("location");
				description = json.getString("description");
				url = json.getString("url");
				profileImageUrl = json.getString("profile_image_url");
				userDomain = json.getString("domain");
				gender = json.getString("gender");
				followersCount = json.getInt("followers_count");
				friendsCount = json.getInt("friends_count");
				statusesCount = json.getInt("statuses_count");
				favouritesCount = json.getInt("favourites_count");
				createdAt = JSONUtility.parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
				following = json.getBoolean("following");				
				allowAllActMsg = json.getBoolean("allow_all_act_msg");
				geoEnabled = json.getBoolean("geo_enabled");			
				verified = json.getBoolean("verified");
				allowAllComment = json.getBoolean("allow_all_comment");
				avatarLarge = json.getString("avatar_large");
				verifiedReason = json.getString("verified_reason");
				followMe = json.getBoolean("follow_me");
				onlineStatus = json.getInt("online_status");
				biFollowersCount = json.getInt("bi_followers_count");
				if (!json.isNull("status")) {
					setStatus(new Status(json.getJSONObject("status")));
				}
			} catch (JSONException jsone) {
				throw new WeiboException(jsone.getMessage() + ":" + json.toString(), jsone, 90000);
			}
		}
	}
	/**
	 * 从服务器返回的json数据中读取User对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return User对象
	 */
	public static User constructUser(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			return new User(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}		
	}
	/**
	 * 从服务器返回的json数据中读取List<User>对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return List<User>对象
	 */
	public static List<User> constructUsers(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			JSONArray list = jsonObj.getJSONArray("users");
			int size = list.length();
			List<User> users = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				users.add(new User(list.getJSONObject(i)));
			}
			return users;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}
	/**
	 * Returns the id of the user
	 *
	 * @return the id of the user
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the name of the user
	 *
	 * @return the name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the screen name of the user
	 *
	 * @return the screen name of the user
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * Returns the location of the user
	 *
	 * @return the location of the user
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Returns the description of the user
	 *
	 * @return the description of the user
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the profile image url of the user
	 *
	 * @return the profile image url of the user
	 */
	public URL getProfileImageURL() {
		try {
			return new URL(profileImageUrl);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Returns the url of the user
	 *
	 * @return the url of the user
	 */
	public URL getURL() {
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Test if the user status is protected
	 *
	 * @return true if the user status is protected
	 */
	public boolean isAllowAllActMsg() {
		return allowAllActMsg;
	}


	public String getUserDomain() {
		return userDomain;
	}

	/**
	 * Returns the number of followers
	 *
	 * @return the number of followers
	 * @since Weibo4J 1.2.1
	 */
	public int getFollowersCount() {
		return followersCount;
	}

	/**
	 * Returns the code of province
	 *
	 * @return the code of province
	 * @since Weibo4J 1.2.1
	 */
	public int getProvince() {
		return province;
	}

	/**
	 * Returns the code of city
	 *
	 * @return the code of city
	 * @since Weibo4J 1.2.1
	 */
	public int getCity() {
		return city;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public int getFavouritesCount() {
		return favouritesCount;
	}

	public String getGender() {
		return gender;
	}

	public boolean isFollowing() {
		return following;
	}

	public int getStatusesCount() {
		return statusesCount;
	}

	/**
	 * @return the user is enabling geo location
	 * @since Weibo4J 1.2.1
	 */
	public boolean isGeoEnabled() {
		return geoEnabled;
	}

	/**
	 * @return returns true if the user is a verified celebrity
	 * @since Weibo4J 1.2.1
	 */
	public boolean isVerified() {
		return verified;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "User{" +
		", id=" + id +
		", screenName='" + screenName + '\'' +
		", name='" + name + '\'' +
		", province='" + province +'\'' +
		", city='" +city +'\''+
		", location='" + location + '\'' +
		", description='" + description + '\'' +
		", url='" + url + '\'' +
		", profileImageUrl='" + profileImageUrl + '\'' +		
		", domain ='" + userDomain+ '\'' +
		", gender ='" + gender + '\'' +
		", followersCount=" + followersCount +
		", friendsCount=" + friendsCount +
		", statusesCount=" + statusesCount +
		", favouritesCount=" + favouritesCount +
		", createdAt=" + createdAt +
		", following=" + following +	
		", allowAllActMsg=" + allowAllActMsg +
		", geoEnabled=" + geoEnabled +		
		", verified=" + verified +
		", allowAllComment" + allowAllComment + '\'' +
		", avatarLarge='" + avatarLarge + '\'' +
		", verifiedReason='" + verifiedReason + '\'' +
		", followMe=" + followMe +
		", onlineStatus=" + onlineStatus +
		", biFollowersCount=" + biFollowersCount +
		", status=" + status +
		'}';
	}

}
