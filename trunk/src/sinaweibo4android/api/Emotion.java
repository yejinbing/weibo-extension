package sinaweibo4android.api;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sinaweibo4android.WeiboException;

public class Emotion {

	private String category;
	private boolean isCommon;
	private boolean isHot;
	private String icon;
	private String phrase;
	private String type;
	private String url;
	private String value;
	
	public Emotion(JSONObject json)throws WeiboException, JSONException{
		this.category = json.getString("category");
		this.isCommon = json.getBoolean("common");
		this.isHot = json.getBoolean("hot");
		this.icon = json.getString("icon");
		this.phrase = json.getString("phrase");
		this.url = json.getString("url");
		this.value = json.getString("value");
	}
	
	public static List<Emotion> constructEmotions(String str) throws WeiboException {
		try {
			JSONArray list = new JSONArray(str);
			int size = list.length();
			List<Emotion> Emotions = new ArrayList<Emotion>(size);
			for (int i = 0; i < size; i++) {
				Emotions.add(new Emotion(list.getJSONObject(i)));
			}
			return Emotions;
		} catch (JSONException e) {
			throw new WeiboException(e.getMessage(), 90000);
		} catch (WeiboException e) {
			throw e;
		}
		
	}
	
	
	public String getCategory() {
		return this.category;
	}
	
	public boolean isCommon() {
		return this.isCommon;
	}
	
	public boolean isHot() {
		return this.isHot;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public String getPhrase() {
		return this.phrase;
	}
	
	public String getType() {
		return this.phrase;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getValue() {
		return this.value;
	}
}
