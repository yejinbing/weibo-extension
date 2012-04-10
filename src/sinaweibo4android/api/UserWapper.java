/*
 * UserObjectWapper.java created on 2010-7-28 上午08:48:35 by bwl (Liu Daoru)
 */

package sinaweibo4android.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sinaweibo4android.WeiboException;

/**
 * 对User对象列表进行的包装，以支持cursor相关信息传递
 * @author sinaWeibo
 */
public class UserWapper implements Serializable {

	private static final long serialVersionUID = -3119107701303920284L;

	/**
	 * 用户对象列表
	 */
	private List<User> users;

	/**
	 * 向前翻页的cursor
	 */
	private long previousCursor;

	/**
	 * 向后翻页的cursor
	 */
	private int nextCursor;

	public UserWapper(List<User> users, long previousCursor, int nextCursor) {
		this.users = users;
		this.previousCursor = previousCursor;
		this.nextCursor = nextCursor;
	}
	
	/**
	 * 从服务器返回的json数据中读取List<User>对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return List<User>对象
	 */
	public static UserWapper constructUserWapper(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			JSONArray list = jsonObj.getJSONArray("users");
			int size = list.length();
			List<User> users = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				users.add(new User(list.getJSONObject(i)));
			}
			int nextCursor = jsonObj.getInt("next_cursor");
			int previousCursor = jsonObj.getInt("previous_cursor");
			return new UserWapper(users, previousCursor, nextCursor);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public long getPreviousCursor() {
		return previousCursor;
	}

	public void setPreviousCursor(long previousCursor) {
		this.previousCursor = previousCursor;
	}

	public int getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(int nextCursor) {
		this.nextCursor = nextCursor;
	}

}
