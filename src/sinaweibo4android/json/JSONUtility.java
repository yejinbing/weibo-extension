package sinaweibo4android.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import sinaweibo4android.WeiboException;

public class JSONUtility {
	private static Map<String,SimpleDateFormat> formatMap = new HashMap<String,SimpleDateFormat>();
	
	public static Date parseDate(String str, String format) throws WeiboException{
        if(str==null||"".equals(str)){
        	return null;
        }
    	SimpleDateFormat sdf = formatMap.get(format);
        if (null == sdf) {
            sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            formatMap.put(format, sdf);
        }
        try {
            synchronized(sdf){
                // SimpleDateFormat is not thread safe
                return sdf.parse(str);
            }
        } catch (ParseException pe) {
            throw new WeiboException("Unexpected format(" + str + ") returned from sina.com.cn");
        }
    }
	
	public static int getInt(String key, JSONObject json) throws JSONException {
        String str = json.getString(key);
        if(null == str || "".equals(str)||"null".equals(str)){
            return -1;
        }
        return Integer.parseInt(str);
    }
	    
	public static long getLong(String key, JSONObject json) throws JSONException {
        String str = json.getString(key);
        if(null == str || "".equals(str)||"null".equals(str)){
            return -1;
        }
        return Long.parseLong(str);
    }
	
	public static boolean getBoolean(String key, JSONObject json) throws JSONException {
        String str = json.getString(key);
        if(null == str || "".equals(str)||"null".equals(str)){
            return false;
        }
        return Boolean.valueOf(str);
    }
}
