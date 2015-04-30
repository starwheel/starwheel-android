//
// OoVooActivity.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.oovoo.core.IConferenceCore.ConferenceCoreError;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.ooVoo.Common.ParticipantVideoSurface;
import net.omplanet.starwheel.ooVoo.Common.Utils;
import net.omplanet.starwheel.ooVoo.ConferenceManager;
import net.omplanet.starwheel.ooVoo.ConferenceManager.SessionListener;
import net.omplanet.starwheel.ooVoo.SessionUIPresenter;
import net.omplanet.starwheel.ooVoo.Settings.SettingsActivity;
import net.omplanet.starwheel.ooVoo.Settings.UserSettings;
import net.omplanet.starwheel.ooVoo.VideoCall.VideoCallActivity;

// Main presenter entity
public class OoVooActivity extends Activity implements OnClickListener, SessionListener, SessionUIPresenter
{

	private static final String		TAG					= OoVooActivity.class.getName();
	private ConferenceManager		mConferenceManager	= null;
//	private EditText				mSessionIdView		= null;
//	private EditText				mDisplayNameView	= null;
	private Button					mJoinButton			= null;
	private ProgressDialog			mWaitingDialog		= null;
	private ParticipantVideoSurface	mPreviewSurface;
	private Boolean					isInitialized		= false;
	private boolean					isJoining			= false;

	public String getAppVersion()
	{
		String versionName = new String();
		try
		{
			versionName = this.getPackageManager().getPackageInfo( this.getPackageName(), 0).versionName;
		} catch( NameNotFoundException e)
		{
			Log.e( TAG, "", e);
		}
		return versionName;
	}

	private void hideAvatar()
	{
		if( mPreviewSurface != null)
		{
			mPreviewSurface.avatar.setVisibility( View.INVISIBLE);
			mPreviewSurface.mVideoView.setVisibility( View.VISIBLE);
		}
	}

	public void hideWaitingMessage()
	{
		try
		{
			if( mWaitingDialog != null)
			{
				mWaitingDialog.dismiss();
			}
			mWaitingDialog = null;
		} catch( Exception ex)
		{
		}
	}

	private void initConferenceManager()
	{
		if( !isInitialized)
		{
			if( mConferenceManager == null)
			{
				Log.i( TAG, "Init ConferenceManager");
				mConferenceManager = ConferenceManager.getInstance( getApplicationContext());
			}

			if( mConferenceManager != null)
			{
				mConferenceManager.removeSessionListener( this);
				mConferenceManager.addSessionListener( this);
				mConferenceManager.initConferenceCore();
			}
		}
	}

	@Override
	public void initSurfaces()
	{
		// TODO Auto-generated method stub

	}

	protected void initView()
	{
		Log.i( TAG, "Setup views ->");
		mConferenceManager.getParticipantsManager().addParticipant( ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT,
				"Me", mPreviewSurface.mVideoView.getId());
		mConferenceManager.getParticipantsManager().setVideoOn( ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT, true);

		/*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			ActionBar ab = getActionBar();
			if( ab != null)
			{
				ab.setIcon( R.drawable.ic_main);
			}
		}*/
		Log.i( TAG, "<- Setup views");
	}

	public boolean isOnline()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
		try
		{
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if( netInfo != null && netInfo.isConnectedOrConnecting())
			{
				return true;
			}
		} catch( Exception e)
		{
			Log.d( Utils.getOoVooTag(), "An exception while trying to find internet connectivity: " + e.getMessage());
			// probably connectivity problem so we will return false
		}
		return false;
	}

	@Override
	public void onBackPressed()
	{
		if( mConferenceManager != null)
			mConferenceManager.leaveSession();
		super.onBackPressed();
	}

	@Override
	public void onClick( View v)
	{
		if( v == null)
			return;

		if( !isOnline())
		{
			Utils.ShowMessageBox(this, "Network Error",
					"No Internet Connection. Please check your internet connection or try again later.");
			return;
		}

/*		if( mDisplayNameView.getText().toString().isEmpty())
		{
			onJoinSessionWrongDataError();

			return;
		}*/

		switch( v.getId())
		{
			case R.id.joinButton1:

				if( mConferenceManager.isSdkInitialized())
				{
					onJoinSession();
				}
				else
				{
					Toast.makeText( getApplicationContext(), R.string.initialization_wait, Toast.LENGTH_SHORT).show();
					initConferenceManager();
				}
				break;
		}
	}

	@Override
	protected void onCreate( Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState);
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Log.d( TAG, "main activity onCreate");

		// Set layout
		setContentView(R.layout.activity_main_oovoo);

		// Register for button press
		Object obj = findViewById( R.id.joinButton1);
		mJoinButton = (Button) obj;
		mJoinButton.setOnClickListener( this);
		mJoinButton.setEnabled( false);

		// Retrieve and display SDK version
//		mSessionIdView = (EditText) findViewById( R.id.sessionIdText);
//		mDisplayNameView = (EditText) findViewById( R.id.displayNameText);

		mPreviewSurface = (ParticipantVideoSurface) findViewById( R.id.preview_layout_id);
		mPreviewSurface.avatar = (ImageView) findViewById( R.id.myAvatar);
		mPreviewSurface.mVideoView = (android.opengl.GLSurfaceView) findViewById( R.id.myVideoSurface);

		try
		{
			initConferenceManager();
			initView();
		} catch( Exception e)
		{
			Log.e( TAG, "onCreate exception: ", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate( R.menu.oovoo_menu, menu);

		return true;
	}

	@Override
	public void onFullModeChanged( int id)
	{
		// TODO Auto-generated method stub

	}

	private synchronized void onJoinSession()
	{

		if( isJoining)
		{
			return;
		}

		isJoining = true;

//		saveSettings();

		// Join session
		mJoinButton.setEnabled(false);
		showWaitingMessage();
		mConferenceManager.joinSession();
	}

	@Override
	public void onJoinSessionError( final ConferenceCoreError error)
	{
		Log.e( TAG, "onJoinSessionError: " + error);
		isJoining = false;
		showErrorMessage(
				"Join Session",
				"Error while trying to join session. "
						+ mConferenceManager.getErrorMessageForConferenceCoreError( error));
	}

	@Override
	public void onJoinSessionSucceeded()
	{
		switchToVideoCall();
		isJoining = false;
	}

	@Override
	public void onJoinSessionWrongDataError()
	{
		showErrorMessage( "Join Session", "Display Name should not be empty");
	}

	@Override
	public void onLeftSession( ConferenceCoreError error)
	{
	}

	@Override
	public void onMultiModeChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item)
	{
		if( item == null)
			return false;

		switch( item.getItemId())
		{
			case R.id.menu_settings:
				if( !isInitialized)
				{
					Toast.makeText( getApplicationContext(), R.string.initialization_wait, Toast.LENGTH_SHORT).show();
				}

				startActivity( SettingsActivity.class);

				return true;
		}
		return super.onOptionsItemSelected( item);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if( isInitialized)
			mConferenceManager.pauseSession();

		mConferenceManager.removeSessionListener( this);

		isInitialized = true;
//		saveSettings();
	}

	@Override
	public synchronized void onResume()
	{
		Log.i(TAG, "main activity onResume ->");
		mConferenceManager.addSessionListener(this);

		// Read settings
		UserSettings settings = mConferenceManager.retrieveSettings();
/*
		// Fill views
		mSessionIdView.setText( settings.SessionID);
		mDisplayNameView.setText( settings.DisplayName);

		Log.i( TAG, "persistSettings ->");
		mConferenceManager.persistSettings( settings);

		Log.i( TAG, "<- persistSettings");
*/

		Log.i(TAG, "loadDataFromSettings ->");
		mConferenceManager.loadDataFromSettings();
		Log.i(TAG, "<- loadDataFromSettings");

		mConferenceManager.setSessionUIPresenter(this);
		mConferenceManager.updateGLView(mPreviewSurface.mVideoView.getId(), this);

		showAvatar();

		if( isInitialized)
		{
			mConferenceManager.resumePreviewSession();
			mJoinButton.setEnabled( true);
		}

		mConferenceManager.setCameraMuted(settings.CameraMuted, true);
		super.onResume();
	}

	@Override
	public void onSessionError( String error)
	{
		showErrorMessage( "Error", error);
		isJoining = false;
	}

	@Override
	public void onSessionIDGenerated( String sSessionId)
	{
		Log.d(Utils.getOoVooTag(), "OnSessionIdGenerated called with: " + sSessionId);
	}

	@Override
	public synchronized void onSessionInited()
	{

		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{

				try
				{
					Log.i( TAG, "loadDataFromSettings ->");
					mConferenceManager.loadDataFromSettings();
					Log.i( TAG, "<- loadDataFromSettings");

					mConferenceManager.resumePreviewSession();

					isInitialized = true;
					if( mJoinButton != null)
						mJoinButton.setEnabled( true);

				} catch( Exception e)
				{
					Log.e( TAG, "", e);
				}
			}
		});
	}
/*
	private void saveSettings()
	{
		if( mConferenceManager != null && mSessionIdView != null && mDisplayNameView != null)
		{
			UserSettings settingsToPersist = mConferenceManager.retrieveSettings();
			settingsToPersist.UserID = android.os.Build.SERIAL;
			settingsToPersist.DisplayName = mDisplayNameView.getText().toString();
			settingsToPersist.SessionID = mSessionIdView.getText().toString();

			// Save changes
			mConferenceManager.persistSettings( settingsToPersist);
		}
	}*/

	private void showAvatar()
	{
		if( mPreviewSurface != null)
			mPreviewSurface.avatar.setVisibility( View.VISIBLE);
	}

	public void showErrorMessage( final String titleToShow, final String msgToShow)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				if( mJoinButton != null)
					mJoinButton.setEnabled( true);
				hideWaitingMessage();
				Utils.ShowMessageBox( OoVooActivity.this, titleToShow, msgToShow);
			}
		});
	}

	private void showWaitingMessage()
	{
		mWaitingDialog = new ProgressDialog( this);
		mWaitingDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER);
		mWaitingDialog.setMessage( getResources().getText( R.string.please_wait));
		mWaitingDialog.setIndeterminate( true);
		mWaitingDialog.setCancelable( false);
		mWaitingDialog.setCanceledOnTouchOutside( false);
		mWaitingDialog.show();
	}

	// Start a new activity using the requested effects
	private void startActivity( Class<?> activityToStart)
	{
		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent( this, activityToStart);
		myIntent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity( myIntent);
	}

	private void switchToVideoCall()
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				hideWaitingMessage();
				startActivity( VideoCallActivity.class);
			}
		});
	}

	@Override
	public void updateParticipantSurface( final int participantViewId, String displayName, final boolean isVideoOn)
	{

		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				hideAvatar();
			}
		});
	}

}
