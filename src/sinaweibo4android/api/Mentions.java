package sinaweibo4android.api;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import sinaweibo4android.WeiboException;


public class Mentions {
	
	private List<Status> statuses = null;
	private int previousCursor;
	private int nextCursor;
	
	public Mentions(List<Status> statuses, int previousCursor, int nextCursor) {
		this.statuses = statuses;
		this.previousCursor = previousCursor;
		this.nextCursor = nextCursor;
	}
	/**
	 * 从服务器返回的json数据中读取Mentions对象
	 * @param str 服务器返回的json数据
	 * @author yejb 2012.3.14
	 * @return Mentions对象
	 */
	public static Mentions constructMentions(String str) throws WeiboException {
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(str);
			List<Status> statuses = Status.constructStatuses(str);
			int previousCursor = jsonObj.getInt("previous_cursor");
			int nextCursor = jsonObj.getInt("next_cursor");
			return new Mentions(statuses, previousCursor, nextCursor);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			// TODO: handle exception
			throw e;
		}
		
	}
	
	public List<Status> getStatues() {
		return statuses;
	}
	
	public int getPreviousCursor() {
		return previousCursor;
	}
	
	public int getNextCursor() {
		return nextCursor;
	}

}
