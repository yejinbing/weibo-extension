/*
 * Copyright 2010 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.googlecode.WeiboExtension.EventStream;

import sinaweibo4android.AccessToken;
import sinaweibo4android.DialogError;
import sinaweibo4android.Weibo;
import sinaweibo4android.WeiboDialogListener;
import sinaweibo4android.WeiboException;
import sinaweibo4android.api.Account;
import sinaweibo4android.api.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.googlecode.WeiboExtension.AccessInfo;
import com.googlecode.WeiboExtension.Constants;
import com.googlecode.WeiboExtension.HomeActivity;
import com.googlecode.WeiboExtension.R;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.Config;
import com.googlecode.WeiboExtension.EventStream.EventStreamConstants.ConfigState;
import com.googlecode.WeiboExtension.EventStream.SNSSamplePluginApplication.State;
import com.googlecode.WeiboExtension.Utility.Utility;
import com.googlecode.WeiboExtension.db.AccountDBAdapter;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY;
import static com.googlecode.WeiboExtension.EventStream.Constants.PLUGIN_KEY_PARAMETER;
/**
 * 
 * This class extends an activity and is used as the configuration activity.
 * It coordinates the login and logout activity of the sample application, 
 * and as such also handles the concerned Dialogs.
 * It implements the OnClickListener, OnCancelListener and 
 * SNSSamplePluginApplication.StateListener
 */
public class SNSSamplePluginConfig extends Activity implements OnClickListener,
        OnCancelListener, SNSSamplePluginApplication.StateListener {

    private static final String TAG = "SNSSamplePluginConfig";

    // Dialog constants used for onCreateDialog
    public static final int DIALOG_LOGIN = 0;
    public static final int DIALOG_LOGOUT = 1;
    public static final int DIALOG_PROGRESS = 2;
    public static final int DIALOG_ERROR = 3;

    // Bundle parameter names for dialog creation
    private final String TITLE = "title";
    private final String MESSAGE = "message";

    private String mErrorMessage = null;

    /**
     * onCreate: Request window title.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Config.DEBUG) {
            Log.d(TAG, "Config, onCreate");
        }
    }

    /**
     * Set up the activity according to the appication state.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Add callbacks
        SNSSamplePluginApplication app = (SNSSamplePluginApplication) getApplication();
        app.addStateListener(this);

        State state = getState();

        if (Config.DEBUG) {
            Log.d(TAG, "onResume:" + state);
        }

        switch (state) {

            case NOT_CONFIGURED:
                setState(State.NOT_AUTHENTICATED);
                break;

            case NOT_AUTHENTICATED:
//                showDialog(DIALOG_LOGIN);
            	addUser();
                break;

            case AUTHENTICATION_IN_PROGRESS:
                showDialog(DIALOG_PROGRESS);
                break;

            case AUTHENTICATION_FAILED:
                setState(State.NOT_AUTHENTICATED);
                break;

            case AUTHENTICATION_SUCCESS:
                setState(State.AUTHENTICATED);
                break;

            case AUTHENTICATED:
                // If activity is started and already authenticated perform logout
                showDialog(DIALOG_LOGOUT);
                break;

            case AUTHENTICATION_BAD_CREDENTIALS:
            case AUTHENTICATION_NETWORK_FAILED:
            case INVALID_ACCOUNT:
//                showDialog(DIALOG_LOGIN);
            	addUser();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeDialog(DIALOG_PROGRESS);
    }

    /**
     * Remove listener functionality when activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Remove callbacks
        SNSSamplePluginApplication app = (SNSSamplePluginApplication) getApplication();
        app.removeStateListener(this);

        closeDialog(DIALOG_PROGRESS);
    }
    /**
     * Prepare the Dialogs that needs to be prepared before showing them.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        if (id == DIALOG_LOGIN) {
        	
        }
        else if (id == DIALOG_ERROR) {
            ((AlertDialog)dialog).setMessage(args.getString(MESSAGE));
        }
    }

    /**
     * Create a dialog of your choice - merely branch out to the correct dialog.
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (Config.DEBUG) {
            Log.d(TAG, "onCreateDialog:" + id);
        }

        switch (id) {
//            case DIALOG_LOGIN:
//                return setupLoginDialog();
            case DIALOG_LOGOUT:
                return setupLogoutDialog();
            case DIALOG_PROGRESS:
                return setupProgressDialog();
            case DIALOG_ERROR:
                return setupErrorDialog();
            default:
                return null;
        }
    }

    /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private Dialog setupErrorDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(R.string.ts_snssample_authentication_failed_title);
        dialog.setMessage("");
        dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setState(State.NOT_AUTHENTICATED);
            }
        });
        return dialog.create();
    }
    /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private Dialog setupLogoutDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.ts_snssample_logout_label);
        dialog.setMessage(R.string.ts_snssample_logout_message);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                callSNSSampleLogout();
                finish();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, this);
        dialog.setOnCancelListener(this);
        return dialog.create();
    }

    /**
     * Set up the concerned dialog
     * @return the newly created Dialog
     */
    private ProgressDialog setupProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.ts_snssample_login_label));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * This methods initiates a logout from the service
     */
    private void callSNSSampleLogout() {
        Intent logoutIntent = new Intent(Constants.LOGOUT_INTENT);
        Context context = getApplicationContext();

        logoutIntent.setClass(context, SNSSampleService.class);
        logoutIntent.putExtra(PLUGIN_KEY_PARAMETER, PLUGIN_KEY);

        startService(logoutIntent);
    }
    /**
     * This method finishes the activity
     */
    private void callCancel() {
        finish();
    }

    /**
     * This method finishes the activity
     */
    public void onClick(DialogInterface dialog, int whichButton) {
        callCancel();
    }

    /**
     * This method finishes the activity
     */
    public void onCancel(DialogInterface arg0) {
        callCancel();
    }

    /**
     * This method is a state handler that takes care of eventual state changes 
     * in the service and the effect it may have on the currently active dialog.
     */
    public void onStateChange(final SNSSampleNotification newState) {
        if (Config.DEBUG) {
            Log.d(TAG, "onStateChange:" + newState.getState());
        }

        Runnable runnable = new Runnable() {
            public void run() {
                switch (newState.getState()) {
                    case NOT_AUTHENTICATED:
                        closeDialog(DIALOG_PROGRESS);
//                        showDialog(DIALOG_LOGIN);
                        addUser();
                        break;

                    case AUTHENTICATION_IN_PROGRESS:
                        showDialog(DIALOG_PROGRESS);
                        break;

                    case INVALID_ACCOUNT:
                        Bundle failedDialogParams = new Bundle();

                        failedDialogParams.putString(TITLE, getResources().getString(
                                R.string.ts_snssample_authentication_failed_title));
                        failedDialogParams.putString(MESSAGE, mErrorMessage);
                        mErrorMessage = null;
                        showDialog(DIALOG_ERROR, failedDialogParams);
                        break;

                    case AUTHENTICATION_FAILED:
                        mErrorMessage = (String) newState.getExtraData();

                        closeDialog(DIALOG_PROGRESS);
                        setState(State.INVALID_ACCOUNT);
                        break;

                    case AUTHENTICATION_BAD_CREDENTIALS:
                        mErrorMessage = (String) newState.getExtraData();

                        closeDialog(DIALOG_PROGRESS);
                        setState(State.INVALID_ACCOUNT);
                        break;

                    case AUTHENTICATION_NETWORK_FAILED:
                        closeDialog(DIALOG_PROGRESS);
                        showDialog(DIALOG_ERROR);
                        break;

                    case AUTHENTICATION_SUCCESS:
                        setState(State.AUTHENTICATED);
                        break;

                    case AUTHENTICATED:
                        closeDialog(DIALOG_PROGRESS);
                        Intent toIntent = new Intent(SNSSamplePluginConfig.this, HomeActivity.class);
    		            startActivity(toIntent);
                        finish();
                        break;
                }
            }
        };
        runOnUiThread(runnable);
    }
    /**
     * Helper method to close indicated Dialog
     * @param id ID of dialog to close
     */
    private void closeDialog(int id) {
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException exception) {

        }
    }

    /**
     * 
     * @param state State indication to set to the application.
     */
    private void setState(State state) {
        ((SNSSamplePluginApplication) getApplication()).setState(state);
    }

    /**
     * This methods gets the application state
     * @return the application state
     */
    private State getState() {
        return ((SNSSamplePluginApplication) getApplication()).getState();
    }
    
    private void addUser() {
   	 // !!Don't forget to set app_key and secret before get
        // token!!!
    	Weibo weibo = Weibo.getInstance();
        weibo.setupConsumerConfig(Constants.SinaConstants.CONSUMER_KEY, 
       		 Constants.SinaConstants.CONSUMER_SECRET);

        // Oauth2.0 隐式授权认证方式
        weibo.setRedirectUrl("http://www.sina.com");
        weibo.authorize(SNSSamplePluginConfig.this, new AuthDialogListener());
    }
    
    class AuthDialogListener implements WeiboDialogListener {
	
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, Constants.SinaConstants.CONSUMER_KEY);
			accessToken.setExpiresIn(expires_in);
			Weibo weibo = Weibo.getInstance();
			weibo.setAccessToken(accessToken);
			
			long userId = 0;
			try {
				Account account = weibo.getAccount(getApplicationContext());
				userId = account.getId();
			} catch (WeiboException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getStatusCode() + ":" + e.getMessage());
			}
			
			if (userId != 0) {
				User user = null;
				AccessInfo accessInfo;
				try {
					user = weibo.showUser(getApplicationContext(), userId);
					accessInfo = AccessInfo.getInstance();
					accessInfo.setUserId(user.getId());
					accessInfo.setAccessToken(token);
					accessInfo.setConsumerKey(Constants.SinaConstants.CONSUMER_SECRET);
					accessInfo.setScreenName(user.getScreenName());
					accessInfo.setWeiboMark(AccountDBAdapter.WEIBO_MARK_SINA);
					
					AccountDBAdapter dbAdapter = AccountDBAdapter.getInstance(getApplicationContext());
					dbAdapter.saveUserToken(accessInfo);					
					dbAdapter.saveUserInfo(user);
					
					Database database = new Database(getApplicationContext());
					SNSsample snsSample = SNSSampleFactory.getSNSSample(getApplicationContext());
					snsSample.authenticate(user.getId(), token, 
							Constants.SinaConstants.CONSUMER_SECRET);
					if (snsSample.isLoggedIn()) {
						Log.d(TAG, "loggedin");
						Utility.displayToast(getApplicationContext(), "loggedin");
			            // Store display name...
			            if (accessInfo.getScreenName() != null) {
			                final String databaseName = getString(R.string.ts_snssample_logout_label)
			                		+ " " + accessInfo.getScreenName();
			                Settings settings = new Settings(getApplicationContext());
			                settings.setOwnId(accessInfo.getUserId());
			                settings.setDisplayName(accessInfo.getScreenName());
			                settings.setToken(accessInfo.getAccessToken());
			                settings.setTokenSecret(accessInfo.getConsumerKey());
			                database.setConfigurationText(databaseName);
			            }
			            // State that we are configured
			            database.setConfigurationState(ConfigState.CONFIGURED);
			        }
					
					SNSSampleNotification notification = null;
			        if (notification == null) {
			            notification = new SNSSampleNotification(State.AUTHENTICATION_SUCCESS, null);
			        }
			        ((SNSSamplePluginApplication) getApplication()).setState(notification);
				} catch (WeiboException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			
		}
	
		public void onWeiboException(WeiboException e) {
			// TODO Auto-generated method stub
			Utility.displayToast(getApplicationContext(), "Auth exception : " + e.getMessage());
		}
	
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Utility.displayToast(getApplicationContext(),  "Auth error : " + e.getMessage());		
		}
	
		public void onCancel() {
			// TODO Auto-generated method stub
			Utility.displayToast(getApplicationContext(), "Auth cancel");
			finish();
		}
	
	}
}
