package com.googlecode.WeiboExtension;

import com.googlecode.WeiboExtension.EventStream.SNSSampleService;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.TimeLineDBAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener,
							OnPreferenceChangeListener{
	
	private static final String TAG = "Settings";
	
	public static final int DIALOG_CLEAR_CACHE = 0;
	
	private Preference clearCache;
	private EditTextPreference maxCache;
	private CheckBoxPreference automaticAlert;	private ListPreference lpInspectionInterval;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
		clearCache = (Preference)findPreference("clear_cache");
		maxCache = (EditTextPreference)findPreference("max_cache");
		automaticAlert = (CheckBoxPreference)findPreference("automatic_alert");
		lpInspectionInterval = (ListPreference)findPreference("inspection_interval");
		clearCache.setOnPreferenceClickListener(this);
		maxCache.setOnPreferenceChangeListener(this);
		automaticAlert.setOnPreferenceChangeListener(this);
		lpInspectionInterval.setOnPreferenceChangeListener(this);
		
		SharedPreferences prefs = getSharedPreferences("com.googlecode.WeiboExtension_preferences", 0);
		String maxCacheNum = prefs.getString("max_cache", null);
		if (maxCacheNum != null) {
			maxCache.setSummary(maxCacheNum + "");
		}else {
			SharedPreferences.Editor prefsEditor = prefs.edit();
    		prefsEditor.putString("max_cache", "20");
    		prefsEditor.commit();
    		maxCache.setSummary("20");
		}
		boolean isAutomaticAlert = prefs.getBoolean("automatic_alert", false);
		if (!isAutomaticAlert) {
			lpInspectionInterval.setEnabled(false);
		}else {
			lpInspectionInterval.setEnabled(true);
		}
	//设置检测间隔的summary
		String inspectionIntervalValue = prefs.getString("inspection_interval", null);
		if (inspectionIntervalValue != null) {
			String[] valueArray = getResources().getStringArray(
					R.array.inspection_interval_entryValues);
			int i = 0;
			for (String str : valueArray) {
				if (str.equals(inspectionIntervalValue)) {
					break;
				}
				i++;
			}
			if (i >= valueArray.length) {
				Log.e(TAG, "onPreferenceChange: no value in the inspection_interval_entryValues");
			}
			String[] entryArray = getResources().getStringArray(
					R.array.inspection_interval_entries);
			lpInspectionInterval.setSummary(entryArray[i]);
		}
		
	}

	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference.getKey().equals("clear_cache")){
			showDialog(DIALOG_CLEAR_CACHE);
			return true;
		}
		return false;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if (preference.getKey().equals("max_cache")){
			maxCache.setSummary(newValue + "");
			return true;
		}else if (preference.getKey().equals("automatic_alert")) {
			boolean isAutomaticAlert = (Boolean) newValue;
			if (isAutomaticAlert) {
				lpInspectionInterval.setEnabled(true);
				SharedPreferences prefs = getSharedPreferences("com.googlecode.WeiboExtension_preferences", 0);
				String str = prefs.getString("inspection_interval", "0");
				long interval = Long.parseLong(str);
			//设置检测的service
				Intent serviceIntent = new Intent(SNSSampleService.INTENT_ACTION_START);
				serviceIntent.putExtra(SNSSampleService.EXTRA_INTERVAL, interval);
				SettingsActivity.this.startService(serviceIntent);
			}else {
				lpInspectionInterval.setEnabled(false);
			//停止自动检测
				Intent serviceIntent = new Intent(SNSSampleService.INTENT_ACTION_STOP);
				SettingsActivity.this.startService(serviceIntent);
			}
			return true;
		}else if (preference.getKey().equals("inspection_interval")) { 
			long interval = Long.parseLong((String) newValue);
		//设置检测间隔的summary
			String[] valueArray = getResources().getStringArray(
					R.array.inspection_interval_entryValues);
			int i = 0;
			for (String str : valueArray) {
				if (str.equals((String) newValue)) {
					break;
				}
				i++;
			}
			if (i >= valueArray.length) {
				Log.e(TAG, "onPreferenceChange: no value in the inspection_interval_entryValues");
			}
			String[] entryArray = getResources().getStringArray(
					R.array.inspection_interval_entries);
			lpInspectionInterval.setSummary(entryArray[i]);
		//设置检测的service
			Intent serviceIntent = new Intent(SNSSampleService.INTENT_ACTION_START);
			serviceIntent.putExtra(SNSSampleService.EXTRA_INTERVAL, interval);
			SettingsActivity.this.startService(serviceIntent);
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		// TODO Auto-generated method stub
		switch (id) {
			case DIALOG_CLEAR_CACHE:
				return setupClearCacheDialog();
			default:
				return super.onCreateDialog(id, args);
		}
		
	}
	
	 /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private Dialog setupClearCacheDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.clear_cache_dialog_msg);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	TimeLineDBAdapter dbAdapter = TimeLineDBAdapter.getInstance(SettingsActivity.this);
    			int count = dbAdapter.cleanup();
    			if (count > 0) {
    				Utility.displayToast(SettingsActivity.this, 
    						count + getResources().getString(R.string.clear_cache_success));
    			}else {
    				Utility.displayToast(SettingsActivity.this, R.string.clear_cache_failure);
    			}
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        return dialog.create();
    }
	
}
