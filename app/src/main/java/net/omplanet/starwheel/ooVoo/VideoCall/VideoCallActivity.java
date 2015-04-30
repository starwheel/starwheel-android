//
// VideoCallActivity.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo.VideoCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import net.omplanet.starwheel.ooVoo.ConferenceManager;
import net.omplanet.starwheel.ooVoo.ConferenceManager.SessionControlsListener;
import net.omplanet.starwheel.ooVoo.ConferenceManager.SessionListener;
import net.omplanet.starwheel.ooVoo.ConferenceManager.SessionParticipantsListener;
import net.omplanet.starwheel.R;
import net.omplanet.starwheel.ooVoo.SessionUIPresenter;
import net.omplanet.starwheel.ooVoo.Common.Participant;
import net.omplanet.starwheel.ooVoo.Common.ParticipantHolder;
import net.omplanet.starwheel.ooVoo.Common.ParticipantHolder.VideoParticipant;
import net.omplanet.starwheel.ooVoo.Common.ParticipantVideoSurface;
import net.omplanet.starwheel.ooVoo.Common.ParticipantVideoSurface.States;
import net.omplanet.starwheel.ooVoo.Common.ParticipantsManager;
import net.omplanet.starwheel.ooVoo.Common.Utils;
import net.omplanet.starwheel.ooVoo.Information.InformationActivity;
import net.omplanet.starwheel.ooVoo.Settings.UserSettings;
import net.omplanet.starwheel.ooVoo.VideoCall.CameraWrapper.CameraState;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IAudioRouteManager;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.device.deviceconfig.VideoFilterData;
import com.oovoo.core.phone.AudioRouteManager.AudioRoute;
import com.oovoo.core.phone.AudioRouteManager.IAudioRouteManagerListener;

// Video presenter entity
public class VideoCallActivity extends Activity implements OnClickListener, SessionControlsListener, SessionListener,
SessionParticipantsListener, SessionUIPresenter, View.OnTouchListener, IAudioRouteManagerListener
{

	private ConferenceManager							mConferenceManager			= null;
	private HashMap<Integer, ParticipantVideoSurface>	_surfaces					= new HashMap<Integer, ParticipantVideoSurface>();
	private ParticipantVideoSurface						mParticipantsVideoSurfaces[];
	private Button										mAudioRoutesButton;
	private Button										mResolutionButton;
	private Button										mCameraButton;
	private VCParticipantsController					mVCParticipantsController	= null;
	private Spinner										mFilterSpinner;
	private String										mActiveFilterId;
	private boolean										_initialized				= false;
	private boolean										isCameraMuted				= false;
	private IAudioRouteManager							mRouteManager				= null;
	private boolean										isRouteChangedManually		= false;

	@Override
	public void finish()
	{
		LogSdk.d( Utils.getOoVooTag(), "VideoCallActivity finish");
		if( mConferenceManager != null)
			mConferenceManager.setUIReadyState( false);
		super.finish();
	}

	private void fireCameraMuted( boolean isMuted)
	{
		if( !isMuted)
		{
			if( mConferenceManager != null)
				mConferenceManager.setActiveFilter( mActiveFilterId);
		}

		mCameraButton.setSelected( isMuted);

		final SurfaceView myVideoSurface = (SurfaceView) findViewById( R.id.myVideoSurface);

		int new_v = isMuted ? SurfaceView.INVISIBLE : SurfaceView.VISIBLE;
		if( myVideoSurface != null)
		{
			myVideoSurface.setVisibility( new_v);
			ParticipantVideoSurface surface = _surfaces.get( myVideoSurface.getId());
			if( surface != null)
			{
				States state = isMuted ? States.STATE_AVATAR : States.STATE_VIDEO;
				surface.setState( state);

				ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
				if( participantsManager.isFullMode() && !participantsManager.isFullMode( mConferenceManager.myId()))
				{
					surface.hideSurface();
					surface.setVisibility( View.INVISIBLE);
				}

				participantsManager.setVideoOn( mConferenceManager.myId(), mConferenceManager.isCameraMuted() ? false
						: true);
			}
		}
		Log.d(Utils.getOoVooTag(), "set visibility to " + isMuted + " new: " + new_v);
	}

	private void fireMicrophoneEnabled( boolean isEnabled)
	{
		Button btn = (Button) findViewById( R.id.microphoneButton);
		btn.setEnabled(isEnabled);
	}

	private void fireMicrophoneMuted( boolean isMuted)
	{
		Button btn = (Button) findViewById( R.id.microphoneButton);
		btn.setSelected( isMuted ? true : false);
	}

	private void fireSpeakersEnabled( boolean isEnabled)
	{
		Button btn = (Button) findViewById( R.id.speakersButton);
		btn.setEnabled(isEnabled);
	}

	private void fireSpeakersMuted( boolean isMuted)
	{
		// Just GUI. SDK calls are in model
		final Button btn = (Button) findViewById( R.id.speakersButton);
		btn.setSelected(isMuted ? true : false);
	}

	public ParticipantHolder getParticipantHolder()
	{
		return mConferenceManager == null || mConferenceManager.getParticipantsManager() == null ? null
				: mConferenceManager.getParticipantsManager().getHolder();
	}

	public void initSession( ParticipantVideoSurface[] mParticipantsVideoSurfaces)
	{
		// Select devices
		try
		{
			mConferenceManager.selectCamera( CameraState.toInt( CameraState.FRONT_CAMERA));
			mConferenceManager.setVideoResolution( CameraResolutionLevel.ResolutionMedium);

			mConferenceManager.addSessionListener( this);
			mConferenceManager.setSessionUIPresenter( this);

			_surfaces.clear();
			Log.i( Utils.getOoVooTag(), "VideoCallActivity :: initSession -> mParticipantsVideoSurfaces length = "
					+ mParticipantsVideoSurfaces.length);
			for( ParticipantVideoSurface mParticipantsVideoSurface : mParticipantsVideoSurfaces)
			{
				mConferenceManager.updateGLView( mParticipantsVideoSurface.mVideoView.getId(), this);
				_surfaces.put( mParticipantsVideoSurface.mVideoView.getId(), mParticipantsVideoSurface);
			}
			mConferenceManager.initSession( mParticipantsVideoSurfaces);
			mConferenceManager.setUIReadyState( true);
		} catch( Exception e)
		{
			Log.e( Utils.getOoVooTag(), "", e);
		}
	}

	@Override
	public void initSurfaces()
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					for (ParticipantVideoSurface surf : _surfaces.values()) {
						surf.showEmptyCell();
					}
				} catch (Exception err) {
					LogSdk.e(Utils.getOoVooTag(), "");
				}
			}
		});
	}

	protected void initView()
	{
		// Set layout
		setContentView( R.layout.video_call);

		Log.d( Utils.getOoVooTag(), "setting cameraButton");
		mCameraButton = (Button) findViewById( R.id.cameraButton);
		registerForContextMenu( mCameraButton);

		mCameraButton.setOnClickListener( new Button.OnClickListener() {
			@Override
			public void onClick( View v)
			{
				v.showContextMenu();
			}
		});

		Log.d( Utils.getOoVooTag(), "setting microphoneButton");
		Button microphoneButton = (Button) findViewById( R.id.microphoneButton);
		microphoneButton.setOnClickListener( this);

		Log.d( Utils.getOoVooTag(), "setting speakersButton");
		Button speakersButton = (Button) findViewById( R.id.speakersButton);
		speakersButton.setOnClickListener( this);

		Log.d( Utils.getOoVooTag(), "setting audioRoutesButton");
		mAudioRoutesButton = (Button) findViewById( R.id.audioRoutesButton);
		registerForContextMenu( mAudioRoutesButton);

		mAudioRoutesButton.setOnClickListener( new Button.OnClickListener() {
			@Override
			public void onClick( View v)
			{
				v.showContextMenu();
			}
		});

		Log.d( Utils.getOoVooTag(), "setting endOfCallButton");
		Button endOfCallButton = (Button) findViewById( R.id.endOfCallButton);
		endOfCallButton.setOnClickListener( this);

		Log.d( Utils.getOoVooTag(), "setting resolutionButton");
		mResolutionButton = (Button) findViewById( R.id.resolutionButton);
		registerForContextMenu( mResolutionButton);

		mResolutionButton.setOnClickListener( new Button.OnClickListener() {
			@Override
			public void onClick( View v)
			{
				v.showContextMenu();
			}
		});

		Log.d( Utils.getOoVooTag(), "setting ParticipantVideoSurfaces");
		mVCParticipantsController = (VCParticipantsController) findViewById( R.id.participants_controller);

		mParticipantsVideoSurfaces = new ParticipantVideoSurface[4];
		mParticipantsVideoSurfaces[0] = (ParticipantVideoSurface) findViewById( R.id.preview_layout_id);
		mParticipantsVideoSurfaces[0].avatar = (ImageView) findViewById( R.id.myAvatar);
		mParticipantsVideoSurfaces[0].nameBox = (TextView) findViewById( R.id.previewName);
		mParticipantsVideoSurfaces[0].mVideoView = (android.opengl.GLSurfaceView) findViewById( R.id.myVideoSurface);

		mParticipantsVideoSurfaces[1] = (ParticipantVideoSurface) findViewById( R.id.user1_layout_id);
		mParticipantsVideoSurfaces[1].avatar = (ImageView) findViewById( R.id.user1Avatar);
		mParticipantsVideoSurfaces[1].nameBox = (TextView) findViewById( R.id.user1Name);
		mParticipantsVideoSurfaces[1].mVideoView = (android.opengl.GLSurfaceView) findViewById( R.id.user1VideoSurface);

		mParticipantsVideoSurfaces[2] = (ParticipantVideoSurface) findViewById( R.id.user2_layout_id);
		mParticipantsVideoSurfaces[2].avatar = (ImageView) findViewById( R.id.user2Avatar);
		mParticipantsVideoSurfaces[2].nameBox = (TextView) findViewById( R.id.user2Name);
		mParticipantsVideoSurfaces[2].mVideoView = (android.opengl.GLSurfaceView) findViewById( R.id.user2VideoSurface);

		mParticipantsVideoSurfaces[3] = (ParticipantVideoSurface) findViewById( R.id.user3_layout_id);
		mParticipantsVideoSurfaces[3].avatar = (ImageView) findViewById( R.id.user3Avatar);
		mParticipantsVideoSurfaces[3].nameBox = (TextView) findViewById( R.id.user3Name);
		mParticipantsVideoSurfaces[3].mVideoView = (android.opengl.GLSurfaceView) findViewById( R.id.user3VideoSurface);

		mParticipantsVideoSurfaces[0].setOnTouchListener( this);
		mParticipantsVideoSurfaces[1].setOnTouchListener( this);
		mParticipantsVideoSurfaces[2].setOnTouchListener( this);
		mParticipantsVideoSurfaces[3].setOnTouchListener( this);

		mVCParticipantsController.onResize();

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			ActionBar ab = getActionBar();
			if( ab != null)
			{
				ab.setHomeButtonEnabled( false);
				ab.setDisplayShowTitleEnabled( true);
				ab.setDisplayShowHomeEnabled( true);
				ab.setDisplayHomeAsUpEnabled( false);
				ab.setDisplayUseLogoEnabled( false);
//				ab.setIcon( R.drawable.ic_main);
			}
		}

		Log.d( Utils.getOoVooTag(), "setting filterSpinner");
		mFilterSpinner = (Spinner) findViewById( R.id.filterSpinner);
		ArrayList<VideoFilterDataWrapper> f_values = new ArrayList<VideoFilterDataWrapper>();
		VideoFilterData[] arr = mConferenceManager.getAvailableFilters();
		VideoFilterDataWrapper none = null;
		for( VideoFilterData d : arr)
		{
			VideoFilterDataWrapper w = new VideoFilterDataWrapper( d);
			if( d.id().equals( Camera.Parameters.EFFECT_NONE))
			{
				none = w;
			}

			f_values.add( w);
		}
		Utils.setSpinnerValues( this, mFilterSpinner, f_values);
		Utils.setSelectedSpinnerValue( mFilterSpinner, none);

		mFilterSpinner.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected( AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				VideoFilterDataWrapper selectedRes = Utils.getSelectedSpinnerValue( mFilterSpinner);
				mActiveFilterId = selectedRes.id();
				mConferenceManager.setActiveFilter( mActiveFilterId);
			}

			@Override
			public void onNothingSelected( AdapterView<?> arg0)
			{
			}
		});

		showAudioRouteButton();
	}

	@Override
	public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if( v == mCameraButton)
		{
			menu.setHeaderTitle( R.string.change_camera);
			menu.add( mCameraButton.getId(), CameraState.toInt( CameraState.FRONT_CAMERA), 0, R.string.front_camera);
			menu.add( mCameraButton.getId(), CameraState.toInt( CameraState.BACK_CAMERA), 0, R.string.back_camera);
			menu.add( mCameraButton.getId(), CameraState.toInt( CameraState.MUTE_CAMERA), 0, R.string.mute_camera);
		}
		else if( v == mResolutionButton)
		{
			menu.setHeaderTitle( R.string.resolution);
			menu.add( mResolutionButton.getId(), ResolutionWrapper.toInt( CameraResolutionLevel.ResolutionLow), 0,
					R.string.low);
			menu.add( mResolutionButton.getId(), ResolutionWrapper.toInt( CameraResolutionLevel.ResolutionMedium), 0,
					R.string.med);
			menu.add( mResolutionButton.getId(), ResolutionWrapper.toInt( CameraResolutionLevel.ResolutionHigh), 0,
					R.string.hi);
			menu.add( mResolutionButton.getId(), ResolutionWrapper.toInt( CameraResolutionLevel.ResolutionHD), 0,
					R.string.hd);
			menu.setGroupCheckable( mResolutionButton.getId(), true, true);

			UserSettings settings = mConferenceManager.retrieveSettings();
			CameraResolutionLevel level = settings.Resolution;
			for( int i = 0; i < menu.size(); ++i)
			{
				MenuItem mi = menu.getItem( i);
				if( mi.getItemId() == ResolutionWrapper.toInt( level))
				{
					mi.setChecked( true);
				}
			}
		}
		else if( v == mAudioRoutesButton)
		{
			menu.setHeaderTitle( R.string.audio_routes);

			Vector<AudioRoute> routes = mRouteManager.getAudioRoutesList();
			if( routes != null && routes.size() > 0)
			{
				for( int i = 0; i < routes.size(); i++)
				{
					menu.add( mAudioRoutesButton.getId(), AudioRouteWrapper.toInt( routes.get( i)), 0, routes.get( i)
							.toString());
					if( routes.get( i).isActive())
					{
						menu.getItem( i).setChecked( true);
					}
				}
			}
			menu.setGroupCheckable( mAudioRoutesButton.getId(), true, true);
		}
	}

	@Override
	public boolean onContextItemSelected( MenuItem item)
	{
		if( item.getGroupId() == mCameraButton.getId())
		{
			CameraResolutionLevel level = mConferenceManager.getCameraResolutionLevel();

			switch( CameraState.fromInt( item.getItemId()))
			{
			case FRONT_CAMERA:
				mConferenceManager.selectCamera( CameraState.toInt( CameraState.FRONT_CAMERA));
				mConferenceManager.setVideoResolution( level);
				mConferenceManager.setCameraMuted( false, true);
				mCameraButton.setSelected( false);
				break;
			case BACK_CAMERA:
				mConferenceManager.selectCamera( CameraState.toInt( CameraState.BACK_CAMERA));
				mConferenceManager.setVideoResolution( level);
				mConferenceManager.setCameraMuted( false, true);
				mCameraButton.setSelected( false);
				break;
			case MUTE_CAMERA:
				mConferenceManager.setCameraMuted( true, true);
				mCameraButton.setSelected( true);
				break;

			default:
				return false;
			}
		}
		else if( item.getGroupId() == mResolutionButton.getId())
		{
			UserSettings settings = mConferenceManager.retrieveSettings();
			settings.Resolution = ResolutionWrapper.fromInt( item.getItemId());
			mConferenceManager.setVideoResolution( settings.Resolution);
			mConferenceManager.persistSettings( settings);

			item.setChecked( true);
		}
		else if( item.getGroupId() == mAudioRoutesButton.getId())
		{
			isRouteChangedManually = true;
			mRouteManager.setRoute( AudioRouteWrapper.fromInt( item.getItemId()));
		}

		return true;
	}

	@Override
	public void onBackPressed()
	{
		mConferenceManager.endOfCall();
		finish();
		super.onBackPressed();
	}

	@Override
	public void onCameraOn()
	{
		Log.d(Utils.getOoVooTag(), "OnCameraOn");

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCameraButton.setSelected(false);
			}
		});
	}

	@Override
	public void onClick( View v)
	{
		// Check which button was pressed
		switch( v.getId())
		{
		case R.id.endOfCallButton:
		{
			mConferenceManager.leaveSession();
			break;
		}
		case R.id.microphoneButton:
		{
			fireMicrophoneEnabled( false);
			mConferenceManager.toggleMicrophoneMute();
			break;
		}
		case R.id.speakersButton:
		{
			fireSpeakersEnabled( false);
			mConferenceManager.toggleSpeakersMute();
			break;
		}
		default:
		{
			break;
		}
		}
	}

	@Override
	protected void onCreate( Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState);
		setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mConferenceManager = ConferenceManager.getInstance( getApplicationContext());
		mConferenceManager.addSessionParticipantsListener( this);
		Log.d( Utils.getOoVooTag(), "savedInstanceState is null: " + (savedInstanceState == null));
		mActiveFilterId = mConferenceManager.getActiveFilter();
		mRouteManager = mConferenceManager.getAudioRouteManager();
		mRouteManager.setListener(this);
		initView();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vc_menu, menu);

		return true;
	}

	@Override
	protected void onDestroy()
	{
		mConferenceManager.removeSessionParticipantsListener( this);

		Log.d( Utils.getOoVooTag(), "VideoCallActivity onDestroy");
		super.onDestroy();
	}

	@Override
	public void onFullModeChanged( final int participantViewId)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1 && _surfaces != null) {
					for (ParticipantVideoSurface surfaceHolder : _surfaces.values()) {
						if (surfaceHolder.mVideoView.getId() == participantViewId) {
							surfaceHolder.setVisibility(View.VISIBLE);
						} else {
							surfaceHolder.hideSurface();
							surfaceHolder.setVisibility(View.INVISIBLE);
						}
						if (surfaceHolder.getVisibility() == View.VISIBLE
								&& surfaceHolder.mVideoView.getId() == R.id.myVideoSurface) {
							showFiltersButton();
						}
					}
				}

				mVCParticipantsController.onModeUpdated(VCParticipantsController.FULL_MODE);
			}
		});
	}

	@Override
	public void onJoinSessionError( ConferenceCoreError error)
	{
	}

	@Override
	public void onJoinSessionSucceeded()
	{
	}

	@Override
	public void onJoinSessionWrongDataError()
	{
	}

	@Override
	public void onLeftSession( ConferenceCoreError eErrorCode)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(Utils.getOoVooTag(), "onSessionLeft (JAVA MF)");
				// Kill the activity so it will not remain in the stack
				finish();
			}
		});
	}

	@Override
	public void onMultiModeChanged()
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (_surfaces != null) {
					boolean needResume = false;
					for (ParticipantVideoSurface surfaceHolder : _surfaces.values()) {

						int viewId = surfaceHolder.mVideoView.getId();
						if (mConferenceManager.isVideoRenderActive(viewId)) {
							surfaceHolder.setVisibility(View.VISIBLE);
							if (mConferenceManager.isVideoOn(viewId)) {
								surfaceHolder.showSurface();
							}
						} else {
							surfaceHolder.setVisibility(View.INVISIBLE);
						}
						if (surfaceHolder.getState() == States.STATE_AVATAR) {
							surfaceHolder.setVisibility(View.VISIBLE);
							surfaceHolder.update();
						} else if (surfaceHolder.getState() == States.STATE_PAUSED) {
							needResume = true;
							surfaceHolder.setVisibility(View.VISIBLE);
							surfaceHolder.setState(States.STATE_VIDEO);
							surfaceHolder.update();
						}
					}
					if (needResume) {
						ParticipantHolder holder = getParticipantHolder();
						if (holder != null) {
							holder.Resume();
						}
					}
				}
				showAudioRouteButton();
				mVCParticipantsController.onModeUpdated(VCParticipantsController.MULTI_MODE);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item)
	{
		if( item == null)
			return false;

		switch( item.getItemId())
		{
		case android.R.id.home:
			mConferenceManager.endOfCall();
			finish();
			return true;
		case R.id.menu_information:
			openInfrormationView();
			return true;
		}
		return super.onOptionsItemSelected( item);
	}

	@Override
	public void onParticipantJoinedSession( final String sParticipantId, final int participantViewId,
			final String sOpaqueString)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				int viewId = mConferenceManager.getViewIdByParticipant( sParticipantId);

				Log.i( Utils.getOoVooTag(), "VideoCallActivity :: " + sParticipantId
						+ " joined to conference;  {participantViewId = " + viewId + " }");
				if( viewId != -1 && _surfaces != null)
				{
					ParticipantVideoSurface surface = _surfaces.get( viewId);
					if( surface != null)
					{
						surface.setVisibility( View.VISIBLE);
						surface.showAvatar();
						surface.setName( sOpaqueString);
						surface.setState( States.STATE_AVATAR);

						if( mConferenceManager.getParticipantsManager().isFullMode())
						{
							surface.hideSurface();
							surface.setVisibility( View.INVISIBLE);
							surface.setState( States.STATE_EMPTY);
							mConferenceManager.turnParticipantVideoOn( sParticipantId);
						}
					}

					mVCParticipantsController.onResize();
				}
			}
		});
	}

	@Override
	public void onParticipantLeftSession( final int participantViewId, final String sParticipantId)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				if( participantViewId != -1)
				{
					ParticipantVideoSurface surface = _surfaces.get( participantViewId);
					if( surface != null)
					{
						surface.setVisibility(View.INVISIBLE);
						surface.showEmptyCell();
						surface.setState( States.STATE_EMPTY);
					}
					mVCParticipantsController.onResize();
				}
			}
		});
	}

	@Override
	public void onParticipantVideoPaused( final int participantViewId)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				if( participantViewId != -1)
				{
					ParticipantVideoSurface surface = _surfaces.get( participantViewId);
					if( surface != null)
					{
						surface.showAvatar();
						surface.showUserStatusInfo();
						surface.setState( States.STATE_PAUSED);
					}
				}
			}
		});
	}

	@Override
	public void onParticipantVideoResumed( final int participantViewId, final String sParticipantId)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				if( participantViewId != -1)
				{
					ParticipantVideoSurface surface = _surfaces.get( participantViewId);
					if( surface != null)
					{
						ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
						Participant participant = participantsManager.getParticipant( sParticipantId);
						surface.showVideo();
						surface.setName( participant.getDisplayName());
						surface.setState( States.STATE_VIDEO);
					}
				}
			}
		});
	}

	@Override
	public void onParticipantVideoTurnedOff( final ConferenceCoreError eErrorCode, final int participantViewId,
			final String sParticipantId)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				if( participantViewId != -1)
				{
					ParticipantVideoSurface surface = _surfaces.get( participantViewId);
					if( surface != null)
					{
						surface.showAvatar();
						surface.hideUserStatusInfo();
						surface.setState( States.STATE_AVATAR);
					}
				}
			}
		});
	}

	@Override
	public void onParticipantVideoTurnedOn( ConferenceCoreError eErrorCode, final String sParticipantId,
			FrameSize frameSize, final int participantViewId, final String displayName)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				int viewId = mConferenceManager.getViewIdByParticipant( sParticipantId);

				if( viewId != -1)
				{
					ParticipantVideoSurface surface = _surfaces.get( viewId);
					if( surface != null)
					{
						ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();

						surface.setName( displayName);

						if( participantsManager.isFullMode() && !participantsManager.isFullMode( sParticipantId))
						{
							surface.hideSurface();
							surface.setVisibility( View.INVISIBLE);
						}
						else
						{
							surface.showVideo();
						}
						surface.setState( States.STATE_VIDEO);
					}
				}
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		isCameraMuted = mConferenceManager.isCameraMuted();
		mConferenceManager.setCameraMuted( true, false);
		mConferenceManager.pauseSession();
		mConferenceManager.removeSessionControlsListener( this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		UserSettings settings = mConferenceManager.retrieveSettings();

		CameraResolutionLevel level = settings.Resolution;
		Log.d( Utils.getOoVooTag(), "Camera resolution is: " + level);

		mConferenceManager.addSessionControlsListener( this);

		if( !_initialized)
		{
			_initialized = true;
			initSession( mParticipantsVideoSurfaces);
		}

		mConferenceManager.resumeSession();

		ParticipantsManager mParticipantsManager = mConferenceManager.getParticipantsManager();
		if( mParticipantsManager.getNoOfVideosOn() > 0)
		{
			ParticipantHolder holder = mParticipantsManager.getHolder();
			SparseArray<VideoParticipant> users = holder.getParticipants();
			for( int i = 0; i < users.size(); i++)
			{
				VideoParticipant vp = users.valueAt( i);
				if( holder.isVideoOn( vp.getParticipantId()))
				{
					int participantViewId = holder.getViewIdByParticipant( vp.getParticipantId());
					ParticipantVideoSurface surface = _surfaces.get( participantViewId);
					if( surface != null)
					{
						surface.showAvatar();
						surface.setState( States.STATE_AVATAR);
					}
				}
			}
		}

		fireMicrophoneMuted( mConferenceManager.isMicMuted());
		fireSpeakersMuted( mConferenceManager.isSpeakerMuted());
		mConferenceManager.setCameraMuted( isCameraMuted, true);
	}

	@Override
	public void onSessionError( String error)
	{
	}

	@Override
	public void onSessionIDGenerated( String sSessionId)
	{
	}

	@Override
	public void onSessionInited()
	{

	}

	// Called from model upon camera mute change
	@Override
	public void onSetCameraMuted( final boolean isMuted)
	{
		Log.d(Utils.getOoVooTag(), "onSetCameraMuted to " + isMuted);

		// Just GUI. SDK calls are in model
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireCameraMuted(isMuted);
			}
		});
	}

	@Override
	public void onSetMicrophoneEnabled( final boolean isEnabled)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				fireMicrophoneEnabled( isEnabled);
			}
		});
	}

	// Called from model upon microphone mute change
	@Override
	public void onSetMicrophoneMuted( final boolean isMuted)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				fireMicrophoneMuted( isMuted);
			}
		});
		Log.d(Utils.getOoVooTag(), "Microphone mute set to: " + isMuted);
	}

	@Override
	public void onSetSpeakersEnabled( final boolean isEnabled)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				fireSpeakersEnabled( isEnabled);
			}
		});
	}

	// Called from model upon speakers mute change
	@Override
	public void onSetSpeakersMuted( final boolean isMuted)
	{
		runOnUiThread( new Runnable() {
			@Override
			public void run()
			{
				fireSpeakersMuted( isMuted);
			}
		});
		Log.d(Utils.getOoVooTag(), "Speakers mute set to: " + isMuted);
	}

	@Override
	public boolean onTouch( View v, MotionEvent event)
	{
		if( v != null)
		{
			if( v.getVisibility() == View.INVISIBLE)
				return false;

			if( _surfaces != null)
			{
				for( ParticipantVideoSurface surfaceHolder : _surfaces.values())
				{
					if( surfaceHolder.getId() == v.getId())
					{
						ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
						String participantId = participantsManager.getParticipantByViewId( surfaceHolder.mVideoView
								.getId());
						if( participantsManager.isFullMode( participantId)
								|| surfaceHolder.getState() != States.STATE_AVATAR
								&& surfaceHolder.getState() != States.STATE_NONE)
						{
							mConferenceManager.switchUIFullMode( surfaceHolder.mVideoView.getId());
						}

						break;
					}
				}
			}
		}
		return false;
	}

	private void openInfrormationView()
	{
		startActivity( InformationActivity.class);
	}

	private void showAudioRouteButton()
	{
		mFilterSpinner.setVisibility( View.GONE);
		mAudioRoutesButton.setVisibility(View.VISIBLE);
	}

	private void showFiltersButton()
	{
		mFilterSpinner.setVisibility( View.VISIBLE);
		mAudioRoutesButton.setVisibility(View.GONE);
	}

	// Start a new activity using the requested effects
	private void startActivity( Class<?> activityToStart)
	{

		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent( this, activityToStart);
		myIntent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity( myIntent);
	}

	@Override
	public void updateParticipantSurface( final int participantViewId, final String displayName, final boolean isVideoOn)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
				ParticipantVideoSurface surface = _surfaces.get(participantViewId);

				if (surface != null) {
					surface.setName(displayName);
					surface.showAvatar();
					surface.setState(States.STATE_AVATAR);

					if (participantsManager.isFullMode()
							&& participantsManager.getViewIdForFullMode() != participantViewId) {
						surface.hideSurface();
						surface.setVisibility(View.INVISIBLE);
						surface.setState(isVideoOn ? States.STATE_PAUSED : States.STATE_AVATAR);
					} else {
						surface.setVisibility(View.VISIBLE);
						if (isVideoOn) {
							surface.showVideo();
							surface.hideUserStatusInfo();
							surface.setState(States.STATE_VIDEO);
						}
					}
				}
			}
		});
	}

	@Override
	public void onAudioRouteChanges( AudioRoute old_route, AudioRoute new_route)
	{
		try
		{
			if( new_route == AudioRoute.Earpiece)
				mAudioRoutesButton.setBackgroundResource( R.drawable.earpiece_selector);
			else if( new_route == AudioRoute.Speaker)
				mAudioRoutesButton.setBackgroundResource( R.drawable.speakers_selector);
			else if( new_route == AudioRoute.Headset)
				mAudioRoutesButton.setBackgroundResource( R.drawable.headset_selector);
			else if( new_route == AudioRoute.Bluetooth)
				mAudioRoutesButton.setBackgroundResource( R.drawable.bluetooth_selector);

			if( !isRouteChangedManually)
			{
				closeContextMenu();
			}
			isRouteChangedManually = false;
		} catch( Exception e)
		{
			LogSdk.e( Utils.getOoVooTag(), "onAudioRouteChange", e);
		}
	}
}
