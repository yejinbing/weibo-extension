package sinaweibo4android.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.R.array;
import sinaweibo4android.WeiboException;
import sinaweibo4android.json.JSONUtility;


/**
 * A data class representing one single status of a user.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Status implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795691786466526420L;

	private String idstr;					//字符串型的微博ID
	private Date createdAt;             	//创建时间
	private long id;                   		//微博ID
	private String text;               		//微博内容
	private String source;              	//微博来源
	private boolean isFavorited;        	//是否已收藏
	private boolean isTruncated;        	//是否被截断
	private long inReplyToStatusId;			//回复ID
	private long inReplyToUserId;			//回复人UID
	private String inReplyToScreenName;		//回复人昵称
	private long mid;                 		//mid
	private String bmiddle_pic = null;      //中等尺寸图片地址
	private String original_pic = null;     //原始图片地址
	private String thumbnail_pic = null;    //缩略图片地址
	private int reposts_count;				//转发数
	private int comments_count;				//评论数
	private array annotations = null;		//微博附加注释信息
	private Object geo = null;				//地理信息字段
	private User user = null;				//微博作者的用户信息字段
	
	
	private Status retweeted_status;    //转发的微博内容
	


	/**
	 * Return the created_at
	 *
	 * @return created_at
	 * @since Weibo4J 1.1.0
	 */

	public Date getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Returns the id of the status
	 *
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Returns the text of the status
	 *
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Returns the source
	 *
	 * @return the source
	 * @since Weibo4J 1.2.1
	 */
	public String getSource() {
		return this.source;
	}


	/**
	 * Test if the status is truncated
	 *
	 * @return true if truncated
	 * @since Weibo4J 1.2.1
	 */
	public boolean isTruncated() {
		return isTruncated;
	}

	/**
	 * Returns the in_reply_tostatus_id
	 *
	 * @return the in_reply_tostatus_id
	 * @since Weibo4J 1.2.1
	 */
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	/**
	 * Returns the in_reply_user_id
	 *
	 * @return the in_reply_tostatus_id
	 * @since Weibo4J 1.2.1
	 */
	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	/**
	 * Returns the in_reply_to_screen_name
	 *
	 * @return the in_in_reply_to_screen_name
	 * @since Weibo4J 1.2.1
	 */
	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}
	/**
	 * Test if the status is favorited
	 *
	 * @return true if favorited
	 * @since Weibo4J 1.2.1
	 */
	public boolean isFavorited() {
		return isFavorited;
	}

	public String getThumbnail_pic() {
		return thumbnail_pic;
	}

	public String getBmiddle_pic() {
		return bmiddle_pic;
	}

	public String getOriginal_pic() {
		return original_pic;
	}
	/**
	 * Return the user
	 *
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 *
	 * @since Weibo4J 1.2.1
	 */
	public boolean isRetweet(){
		return null != retweeted_status;
	}

	public Status getRetweeted_status() {
		return retweeted_status;
	}

	public long getMid() {
		return mid;
	}
	/**
	 * add by yejinbing,2012.3.9
	 * @return
	 */
	public int getRepostsCount() {
		return reposts_count; 
	}
	/**
	 * add by yejinbing,2012.3.9
	 * @return
	 */
	public int getCommentsCount() {
		return comments_count;
	}
	
	public Status(JSONObject json)throws WeiboException{
		constructJson(json);
	}

	private void constructJson(JSONObject json) throws WeiboException {
		try {
			idstr = json.getString("idstr");
			createdAt = JSONUtility.parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
			id = json.getLong("id");
			text = json.getString("text");
			source = json.getString("source");
			isFavorited = json.getBoolean("favorited");
			isTruncated = json.getBoolean("truncated");
			inReplyToStatusId = JSONUtility.getLong("in_reply_to_status_id", json);
			inReplyToUserId = JSONUtility.getLong("in_reply_to_user_id", json);
			inReplyToScreenName = json.getString("in_reply_to_screen_name");
			mid = json.getLong("mid");
			if (!json.isNull("thumbnail_pic")) {
				thumbnail_pic = json.getString("thumbnail_pic");
				bmiddle_pic = json.getString("bmiddle_pic");
				original_pic = json.getString("original_pic");
			}		
			reposts_count = json.getInt("reposts_count");
			comments_count = json.getInt("comments_count");
			String geo= json.getString("geo");
			if(geo!=null &&!"".equals(geo) &&!"null".equals(geo)){
//				getGeoInfo(geo);
			}
			if(!json.isNull("user"))
				user = new User(json.getJSONObject("user"));
			if(!json.isNull("retweeted_status")){
				retweeted_status= new Status(json.getJSONObject("retweeted_status"));
			}		
		} catch (JSONException e) {
			throw new WeiboException(e.getMessage(), 90000);
		}
	}
	
/*	private void getGeoInfo(String geo) {
		StringBuffer value= new StringBuffer();
		for(char c:geo.toCharArray()){
			if(c>45&&c<58){
				value.append(c);
			}
			if(c==44){
				if(value.length()>0){
					latitude=Double.parseDouble(value.toString());
					value.delete(0, value.length());
				}
			}
		}
		longitude=Double.parseDouble(value.toString());
	}*/
	
	/**
	 * 从服务器返回的json数据中读取List<Statuse>对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return List<Statuse>对象
	 */
	public static List<Status> constructStatuses(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			JSONArray list = jsonObj.getJSONArray("statuses");
			int size = list.length();
			List<Status> statuses = new ArrayList<Status>(size);
			for (int i = 0; i < size; i++) {
				statuses.add(new Status(list.getJSONObject(i)));
			}
			return statuses;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}
	/**
	 * 从服务器返回的json数据中读取Statuse对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return Statuse对象
	 */
	public static Status constructStatus(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			return new Status(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}
	
	@Override
	public String toString() {
		return "Status [createdAt=" + createdAt + ", id=" + id + ", text="
		+ text + ", source=" + source + ", isTruncated=" + isTruncated
		+ ", inReplyToStatusId=" + inReplyToStatusId
		+ ", inReplyToUserId=" + inReplyToUserId + ", isFavorited="
		+ isFavorited + ", inReplyToScreenName=" + inReplyToScreenName
//		+ ", latitude=" + latitude + ", longitude=" + longitude
		+ ", thumbnail_pic=" + thumbnail_pic + ", bmiddle_pic="
		+ bmiddle_pic + ", original_pic=" + original_pic
		+ ",  mid=" + mid + ", user=" + user 
		+", retweeted_status="+(retweeted_status==null?"null":retweeted_status.toString())+ 
		"]";
	}
}
