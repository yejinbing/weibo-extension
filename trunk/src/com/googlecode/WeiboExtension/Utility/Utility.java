package com.googlecode.WeiboExtension.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.googlecode.WeiboExtension.R;
import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

public class Utility {
	
	public static void displayToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void displayToast(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
	
	public static String getDateFormat(Context context, Date date) {
		SimpleDateFormat dfHM = new SimpleDateFormat("HH:mm");
		SimpleDateFormat dfMDHM = new SimpleDateFormat("MM-dd HH:mm");
		
		String timeTrack = null;
		Resources resources = context.getResources();
		long current = System.currentTimeMillis();
		
		Date dateNow = new Date(current);
		long between = (current - date.getTime())/1000;
		
		if (dateNow.getDay() != date.getDay()) {
			timeTrack = dfMDHM.format(date);
		} else if (between < 60) {
			timeTrack = between + resources.getString(R.string.seconds_ago);
		}else if ((between = between/60) < 60) {				
			timeTrack = between + resources.getString(R.string.minutes_ago);
		}else if ((between = between/60) < 2) {
			timeTrack = between + resources.getString(R.string.hours_ago);
		}else if ((between = between/60) < 24) {
			timeTrack = resources.getString(R.string.today) + dfHM.format(date);
		}
		
		return timeTrack;
	}

	public static String convertUrlToFilename(String url) {
		return MD5.toHexString(url.toCharArray());
	}
}
