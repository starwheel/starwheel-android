//
// UserSettingsManager.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.ooVoo.Common.Utils;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.LogLevel;

// Manages saving & loading of the UserSettings class
public class UserSettingsManager
{
	private static final String BASE_BE_URL_DEFAULT = "https://api-sdk.oovoo.com";
	private static UserSettings mSettings;
	public Context mContext;

	public UserSettingsManager(Context context)
	{
		mContext = context;
	}

	// Retrieves user settings
	public UserSettings retrieveSettings()
	{
		if (mSettings == null)
		{
			Log.d(Utils.getOoVooTag(), "Reading user settings from repository...");
			String AppId = null;
			String AppToken = null;
			try
			{
				ApplicationInfo ai;
				ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				AppId = bundle.getString(IConferenceCore.AppIdProp);
				AppToken = bundle.getString( IConferenceCore.AppTokenProp);
				Log.d(Utils.getOoVooTag(), "Retrieved App meta-data settings: AppId = " + AppId + " AppToken = " + AppToken);
			} catch (NameNotFoundException e)
			{
				Log.e(Utils.getOoVooTag(), "Error retrieving configuration!");
				e.printStackTrace();
			}
		    mSettings = new UserSettings();
			SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.ooVooUserSettings), Context.MODE_PRIVATE);
			mSettings.BaseURL = sharedPref.getString(mContext.getResources().getString(R.string.base_url_settings_field), "");
			mSettings.SessionID = sharedPref.getString(mContext.getResources().getString(R.string.session_id_settings_field), "");
			mSettings.AppId = sharedPref.getString(mContext.getResources().getString(R.string.appIdPersisted), AppId);
			mSettings.AppToken = sharedPref.getString(mContext.getResources().getString(R.string.appTokenPersisted), AppToken);
			mSettings.UserID=sharedPref.getString(mContext.getResources().getString(R.string.usrID), "");
			mSettings.CameraMuted=sharedPref.getBoolean(mContext.getResources().getString(R.string.cameraMuted), false);
			mSettings.DisplayName=sharedPref.getString(mContext.getResources().getString(R.string.displayName), "");
			String res=sharedPref.getString(mContext.getResources().getString(R.string.resolution), CameraResolutionLevel.ResolutionMedium.toString());
			mSettings.Resolution=CameraResolutionLevel.valueOf(res);
			mSettings.CurrentLogLevel = LogLevel.fromString(sharedPref.getString(mContext.getResources().getString(R.string.log_level), LogLevel.Trace.toString()));

			if(mSettings.BaseURL.equals(""))
			{
				mSettings.BaseURL = BASE_BE_URL_DEFAULT;
			}
			if(mSettings.UserID.equals(""))
			{
				mSettings.UserID = "TestUser";
			}
			if(mSettings.DisplayName.equals(""))
			{
				mSettings.DisplayName = "TestUserName";
			}
			if(mSettings.SessionID.equals(""))
			{
				mSettings.SessionID = "TEST_SESSION_123";
			}
			if(mSettings.AppId.equals(""))
			{
				mSettings.AppId="9983350480";
			}
			if(mSettings.AppToken.equals(""))
			{
				mSettings.AppId="MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACzL65x/X8c0RKpJ4E+cAArZQ/qhm1SsUdPAn4opnTtCp2j6xuvd4rIOL2OI6UOwXEWPb6+C4ukc1GOxGqwykh4RLB6DFgifGQNeAM2CtUISQ==";
			}
			if(mSettings.CurrentLogLevel == LogLevel.None )
			{
				mSettings.CurrentLogLevel = LogLevel.Debug;
			}
		}
		Log.d(Utils.getOoVooTag(), "Retrieved user settings: " + mSettings);
		return mSettings.Clone();
	}

	// Commits user settings
	public void persistSettings(UserSettings toPersist)
	{
		// Check if need to update
		if (!toPersist.equals(mSettings))
		{
			mSettings = toPersist;
			Log.d(Utils.getOoVooTag(), "Persisting user settings: " + toPersist);
			SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.ooVooUserSettings), Context.MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = sharedPref.edit();
			prefEditor.putString(mContext.getResources().getString(R.string.base_url_settings_field), toPersist.BaseURL);
			prefEditor.putString(mContext.getResources().getString(R.string.session_id_settings_field), toPersist.SessionID);
			prefEditor.putString(mContext.getResources().getString(R.string.appIdPersisted), toPersist.AppId);
			prefEditor.putString(mContext.getResources().getString(R.string.appTokenPersisted), toPersist.AppToken);
			prefEditor.putString(mContext.getResources().getString(R.string.usrID), toPersist.UserID);
			prefEditor.putBoolean(mContext.getResources().getString(R.string.cameraMuted), toPersist.CameraMuted);
			prefEditor.putString(mContext.getResources().getString(R.string.displayName), toPersist.DisplayName);
			prefEditor.putString(mContext.getResources().getString(R.string.log_level), toPersist.CurrentLogLevel.toString());
			prefEditor.commit();
		}
	}

	public void destroy() {
		mSettings = null;
		mContext = null;
	}
}
