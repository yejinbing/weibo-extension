package sinaweibo4android.api;

import org.json.JSONException;
import org.json.JSONObject;

import sinaweibo4android.WeiboException;

public class Error {
	
	private String request = null;
	private int errorCode;
	private String errorMsg = null;

	public Error(JSONObject json) throws WeiboException {
		if(json!=null){
			try {
				request = json.getString("request");
				String error = json.getString("error");
				errorCode = json.getInt("error_code");
				errorMsg = error;
			}
			catch (JSONException jsone) {
				throw new WeiboException(jsone.getMessage(), 90000);
			}
		}
	}
	/**
	 * 从服务器返回的json数据中读取Error对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.12
	 * @return Error对象
	 */
	public static Error constructError(String str) throws WeiboException{
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			return new Error(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}	
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public String getRequest() {
		return request;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Error{" +
		", request=" + request + '\'' +
		", errorCode='" + errorCode + '\'' +
		", errorMsg='" + errorMsg + '\'' +
		'}';
	}
}
