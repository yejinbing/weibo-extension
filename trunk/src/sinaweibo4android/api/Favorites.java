package sinaweibo4android.api;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import sinaweibo4android.WeiboException;
import sinaweibo4android.json.JSONUtility;

public class Favorites {

	private Status status = null;
	private Date favoritedTime = null;			//创建时间
	
	public Favorites(Status status, Date favoritedTime) {
		this.status = status;
		this.favoritedTime = favoritedTime;
	}
	
	/**
	 * 从服务器返回的json数据中读取Favorites对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return Favorites对象
	 */
	public static Favorites constructFavorites(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			Status status = new Status(jsonObj.getJSONObject("status"));
			Date favoritedTime = JSONUtility.parseDate(jsonObj.getString("favorited_time"),
					"EEE MMM dd HH:mm:ss z yyyy"); 
			return new Favorites(status, favoritedTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}
	
	public Status getStatus() {
		return status;
	}
	
	public Date getFavoritedTime() {
		return favoritedTime;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Favorites [status=" + status.toString() + '\'' +
		"favoritedTime=" + favoritedTime.toString() + 
		"]";
	}
	
	
}
