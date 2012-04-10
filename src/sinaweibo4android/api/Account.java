package sinaweibo4android.api;

import org.json.JSONException;
import org.json.JSONObject;
import sinaweibo4android.WeiboException;

public class Account {
	
	private long id;
	
	
	public Account(JSONObject json) throws WeiboException {
		if(json!=null){
			try {
				id = json.getLong("uid");
			} catch (JSONException e) {
				throw new WeiboException(e.getMessage(), 90000);
			}
		}
	}
	/**
	 * 从服务器返回的json数据中读取Account对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return Account对象
	 */
	public static Account constructAccount(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			return new Account(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);		
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}		
	}

	public long getId() {
		return id;
	}
	
	public String toString() {
		return "Account{" +
		", id=" + id +
		'}';
	}
}
