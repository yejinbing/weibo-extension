package sinaweibo4android.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sinaweibo4android.WeiboException;
import sinaweibo4android.json.JSONUtility;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A data class representing one single status of a user.
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class Comment {

	private Date createdAt;                     //评论时间
	private long id;                            //评论id
	private String text;                        //评论内容
	private String source;                      //内容来源
	private long mid;							//评论的MID
	private User user = null;					//评论作者的用户信息字段
	private Status status = null;				//评论的微博信息字段
	private Comment replyComment = null;  		//回复的评论内容


	/* modify by hezhou add some field*/
	public Comment(JSONObject json)throws WeiboException, JSONException{
		createdAt = JSONUtility.parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
		id = json.getLong("id");
		text = json.getString("text");
		source = json.getString("source");
		mid = json.getLong("mid");
		if(!json.isNull("user"))
			user = new User(json.getJSONObject("user"));
		if(!json.isNull("status"))
			status = new Status(json.getJSONObject("status"));
		if(!json.isNull("reply_comment"))
			replyComment = (new Comment(json.getJSONObject("reply_comment")));
	}
	
	public static List<Comment> constructComments(String str) throws WeiboException {
		try {
			JSONObject jsonObj = new JSONObject(str);
			JSONArray list = jsonObj.getJSONArray("comments");
			int size = list.length();
			List<Comment> comments = new ArrayList<Comment>(size);
			for (int i = 0; i < size; i++) {
				comments.add(new Comment(list.getJSONObject(i)));
			}
			return comments;
		} catch (JSONException e) {
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			throw e;
		}
		
	}
	/**
	 * Return the created_at
	 *
	 * @return created_at
	 * @since Weibo4J 1.2.1
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
	
	public long getMid() {
		return mid;
	}
	/**
	 * Return the user
	 *
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	public Status getStatus() {
		return status;
	}

	public Comment getreplyComment() {
		return replyComment;
	}

	@Override
	public String toString() {
		return "Comment{" +
		"createdAt=" + createdAt +
		", id=" + id +
		", text='" + text + '\'' +
		", source='" + source + '\'' +
		", user=" + user +
		", status=" + status +
		'}';
	}
}
