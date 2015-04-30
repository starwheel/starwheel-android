//
// ConferenceManager.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.WindowManager;

import net.omplanet.starwheel.ooVoo.Common.Participant;
import net.omplanet.starwheel.ooVoo.Common.ParticipantVideoSurface;
import net.omplanet.starwheel.ooVoo.Common.ParticipantsManager;
import net.omplanet.starwheel.ooVoo.Common.Utils;
import net.omplanet.starwheel.ooVoo.Settings.MediaDeviceWrapper;
import net.omplanet.starwheel.ooVoo.Settings.UserSettings;
import net.omplanet.starwheel.ooVoo.Settings.UserSettingsManager;
import net.omplanet.starwheel.ooVoo.VideoCall.CameraWrapper.CameraState;
import net.omplanet.starwheel.ooVoo.util.CommandQueued;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.ConferenceCore.IMediaDeviceInformation;
import com.oovoo.core.ConferenceCore.MediaDevice;
import com.oovoo.core.ConferenceCore.MediaDeviceInfo.DeviceState;
import com.oovoo.core.IAudioRouteManager;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.IConferenceCore.ConnectionStatistics;
import com.oovoo.core.IConferenceCore.DeviceType;
import com.oovoo.core.IConferenceCore.LogLevel;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.ILoggerListener;
import com.oovoo.core.Exceptions.CoreException;
import com.oovoo.core.Exceptions.DeviceNotSelectedException;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.device.deviceconfig.VideoFilterData;

public class ConferenceManager implements IConferenceCoreListener, ILoggerListener
{

	public static interface SessionControlsListener
	{
		public void onCameraOn();

		public void onSetCameraMuted( boolean isMuted);

		public void onSetMicrophoneEnabled( boolean isEnabled);

		public void onSetMicrophoneMuted( boolean isMuted);

		public void onSetSpeakersEnabled( boolean isEnabled);

		public void onSetSpeakersMuted( boolean isMuted);
	}

	public static interface SessionListener
	{

		public void onJoinSessionError( ConferenceCoreError error);

		public void onJoinSessionSucceeded();

		public void onJoinSessionWrongDataError();

		public void onLeftSession( ConferenceCoreError error);

		public void onSessionError( String error);

		public void onSessionIDGenerated( String sSessionId);

		public void onSessionInited();
	}

	public static interface SessionParticipantsListener
	{

		public void onParticipantJoinedSession( String sParticipantId, int participantViewId, String sOpaqueString);

		public void onParticipantLeftSession( int participantViewId, String sParticipantId);

		public void onParticipantVideoPaused( int participantViewId);

		public void onParticipantVideoResumed( int participantViewId, String sParticipantId);

		public void onParticipantVideoTurnedOff( ConferenceCoreError errorCode, int participantViewId,
				String sParticipantId);

		public void onParticipantVideoTurnedOn( ConferenceCoreError errorCode, String sParticipantId,
				FrameSize frameSize, int participantViewId, String displayName);
	}

	public static ConferenceManager getInstance( Context app)
	{
		if( instance == null)
		{
			instance = new ConferenceManager( app);
		}
		return instance;
	}

	private static final int					JOIN_SESSION						= 0;
	private static final int					LEAVE_SESSION						= 1;
	private static final int					MUTE_CAMERA							= 2;
	private static final int					MUTE_SPEAKERS						= 3;
	private static final int					MUTE_MIC							= 4;
	private static final int					END_SESSION							= 5;
	private static final int					VIDEO_RESOLUTION					= 6;
	private static final int					TURN_ON_PARTICIPANT_VIDEO			= 7;
	private static final int					TURN_OFF_PARTICIPANT_VIDEO			= 8;
	private static final int					INIT_CONFERENCE_CORE				= 9;
	private static final int					UI_SWITCH_CONFERENCE_MODE			= 10;

	private static final int					UI_PREPARE_HOLDER_USER				= 11;
	private static final int					UI_READY							= 12;
	private static final int					SWITCH_CAMERA						= 13;
	private static final int					SELECT_CAMERA						= 14;
	public static final String					LOCAL_PARTICIPANT_ID_DEFAULT		= "";
	private UserSettingsManager					mSettingsManager;
	private static final String					TAG									= ConferenceManager.class
			.getSimpleName();
	private CommandQueued						mConferenceQueue					= null;
	private static ConferenceManager			instance;
	private Context								mApp								= null;
	private List<SessionListener>				mSessionListenerList				= null;
	private List<SessionParticipantsListener>	mSessionParticipantsListenerList	= null;
	private List<SessionControlsListener>		mSessionControlsListenerList		= null;
	private SessionUIPresenter					mSessionUIPresenter					= null;
	private ParticipantsManager					mParticipantsManager				= null;
	private IConferenceCore						mConferenceCore						= null;
	private boolean								mIsCameraMute						= false;
	private boolean								mIsMicrophoneMute					= false;
	private boolean								mAreSpeakersMute					= false;
	private ArrayList<Message>					mDelayedMessages;
	private boolean								isUIReadyState						= false;

	private boolean								isSdkInitialize						= false;

	private FileLogger							mFileLogger							= null;

	private SurfaceView							mSurfaceView						= null;

	private String								mMySessionId						= ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT;

	private ConferenceManager(Context app)
	{
		mApp = app;

		mMySessionId = ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT;

		Log.i( TAG, "Init SettingsManager ");
		mSettingsManager = new UserSettingsManager( mApp);

		Log.i( TAG, "Init ConferenceQueue ");
		if( mConferenceQueue == null)
		{
			try
			{
				mConferenceQueue = new CommandQueued( "ConferenceManager") {
					@Override
					protected void onHandleCommandMessage( Message msg)
					{
						onConferenceEvent( msg);
					}
				};
			} catch( Exception e)
			{
				Log.e( TAG, "", e);
			}
		}

		mFileLogger = new FileLogger();
		mParticipantsManager = new ParticipantsManager();

		Log.i( TAG, "Init done. ConferenceManager created.");
	}

	public void addSessionControlsListener( SessionControlsListener listener)
	{
		if( mSessionControlsListenerList == null)
			mSessionControlsListenerList = new CopyOnWriteArrayList<SessionControlsListener>();
		mSessionControlsListenerList.add(listener);
	}

	public void addSessionListener( SessionListener l)
	{
		if( mSessionListenerList == null)
			mSessionListenerList = new CopyOnWriteArrayList<SessionListener>();
		if( !mSessionListenerList.contains( l))
			mSessionListenerList.add( l);
	}

	public void addSessionParticipantsListener( SessionParticipantsListener l)
	{
		if( mSessionParticipantsListenerList == null)
		{
			mSessionParticipantsListenerList = new CopyOnWriteArrayList<SessionParticipantsListener>();
		}
		if( !mSessionParticipantsListenerList.contains( l))
			mSessionParticipantsListenerList.add( l);
	}

	private SurfaceView createSurfaceView()
	{
		if( mSurfaceView == null)
		{
			mSurfaceView = new SurfaceView( mApp);
			WindowManager.LayoutParams params = new WindowManager.LayoutParams();
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			params.gravity = Gravity.TOP | Gravity.START;
			params.width = 1;
			params.height = 1;
			WindowManager wmgr = (WindowManager) mApp.getSystemService( Context.WINDOW_SERVICE);
			wmgr.addView( mSurfaceView, params);
		}

		return mSurfaceView;
	}

	public void destroy()
	{
		try
		{
			mConferenceCore.removeListener();
			if( mConferenceQueue != null)
			{
				mConferenceQueue.end();
				mConferenceQueue.destroy();
				mConferenceQueue = null;
			}
			if( mSettingsManager != null)
			{
				mSettingsManager.destroy();
				mSettingsManager = null;
			}
			if( mParticipantsManager != null)
			{
				mParticipantsManager.destroy();
				mParticipantsManager = null;
			}
			if( mSessionListenerList != null)
			{
				mSessionListenerList.clear();
			}
			mSessionListenerList = null;

			if( mSessionParticipantsListenerList != null)
			{
				mSessionParticipantsListenerList.clear();
			}
			mSessionParticipantsListenerList = null;

			if( mSessionControlsListenerList != null)
			{
				mSessionControlsListenerList.clear();
			}
			mSessionControlsListenerList = null;

			if( mFileLogger != null)
			{
				mFileLogger.Stop();
			}
			mFileLogger = null;

			mSessionUIPresenter = null;
			mConferenceCore = null;
			instance = null;
			mApp = null;
		} catch( Exception ex)
		{
			Log.e( TAG, "", ex);
		}
	}

	public void doEndOfCall()
	{
		isUIReadyState = false;
		mConferenceCore.leaveConference( ConferenceCoreError.OK);
	}

	private void doJoinSession()
	{
		UserSettings settings = retrieveSettings();

		settings.Resolution = CameraResolutionLevel.ResolutionMedium;
		persistSettings( settings);

		Log.i( TAG, "Reset Camera Resolution Level to " + settings.Resolution);

		String conferenceID = settings.SessionID;
		String displayName = settings.DisplayName;
		if( displayName.trim().equals( ""))
		{
			if( mSessionListenerList != null)
			{
				for( SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionWrongDataError();
				}
			}
			return;
		}
		Log.d( Utils.getOoVooTag(), "trying to join session " + conferenceID);

		ConferenceCoreError error = mConferenceCore.joinConference( conferenceID, LOCAL_PARTICIPANT_ID_DEFAULT,
				displayName);
		Log.d(Utils.getOoVooTag(), "JoinSession rc = " + error);

		switch( error)
		{
		case OK:
			break;
		case AlreadyInSession:
			if( mSessionListenerList != null)
			{
				for( SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionError( error);
				}
			}
			mConferenceCore.leaveConference( ConferenceCoreError.OK);
			break;

		case ConferenceIdNotValid:
		case ClientIdNotValid:
		case ServerAddressNotValid:
		default:
			if( mSessionListenerList != null)
			{
				for( SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionError( error);
				}
			}
			break;
		}
	}

	private void doLeaveSession()
	{
		ConferenceCoreError rc = mConferenceCore.leaveConference( ConferenceCoreError.OK);
		Log.d( Utils.getOoVooTag(), "Leave session rc = " + rc);
	}

	private void doSetVideoResolution( CameraResolutionLevel resolution)
	{
		try
		{
			Log.d( TAG, "trying to set resolution " + resolution);
			mConferenceCore.setCameraResolutionLevel( resolution);
		} catch( Exception ex)
		{
			Log.e( TAG, "", ex);
		}
	}

	private void doSelectCamera( int cameraId)
	{
		try
		{
			Log.d( TAG, "select camera " + cameraId);
			mConferenceCore.selectCamera( cameraId);
		} catch( Exception ex)
		{
			Log.e( TAG, "", ex);
		}
	}

	private void doSwitchUIFullMode( int viewId)
	{
		if( !mParticipantsManager.isFullMode())
		{
			mParticipantsManager.moveToFullMode( viewId);
			mSessionUIPresenter.onFullModeChanged( viewId);
		}
		else
		{
			mParticipantsManager.moveToMultiMode( viewId);
			mSessionUIPresenter.onMultiModeChanged();
		}
	}

	private void doTurnParticipantVideoOff( String id, String displayName)
	{
		ConferenceCoreError err = ConferenceCoreError.OK;
		Log.d( TAG, "turning participant video OFF for " + id + " MyId = " + mMySessionId);
		if( id.equals( mMySessionId))
		{
			setCameraMuted( true, true);
		}
		else
		{
			err = mConferenceCore.receiveParticipantVideoOff( id);
			Log.d( TAG, "turning participant video OFF rc = " + err + " id = " + id + " " + displayName);
		}

		if( ConferenceCoreError.OK == err)
		{
			mParticipantsManager.turnVideoOff( id);
		}
	}

	private ConferenceCoreError doTurnParticipantVideoOn( String id)
	{
		ConferenceCoreError err = ConferenceCoreError.OK;
		Log.d( TAG, "turning participant video ON for " + id + " MyId = " + mMySessionId);
		if( id.equals( mMySessionId))
			setCameraMuted( false, true);
		else
		{
			err = mConferenceCore.receiveParticipantVideoOn( id);
			Log.d( TAG, "turning participant video ON rc = " + err + " id = " + id);
		}

		return err;
	}

	// Start End of Call logic
	public void endOfCall()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( END_SESSION);
	}

	public String getActiveFilter()
	{
		String id = null;
		if( mConferenceCore != null)
			id = mConferenceCore.getActiveVideoFilter();
		LogSdk.d( TAG, "Get Active Filter to " + id);
		return id;
	}

	public Participant[] getActiveUsers()
	{
		Participant[] rc = new Participant[1];

		if( mParticipantsManager != null)
		{
			List<Participant> users = mParticipantsManager.getParticipants();
			for( int i = 0; i < users.size(); i++)
				if( users.get( i).getId().equals( mMySessionId))
					users.remove( i);
			rc = users.toArray( new Participant[users.size()]);
		}
		return rc;
	}

	public IAudioRouteManager getAudioRouteManager()
	{
		return mConferenceCore.getAudioRouteManager();
	}

	public VideoFilterData[] getAvailableFilters()
	{
		return mConferenceCore.getAvailableVideoFilters();
	}

	public CameraResolutionLevel getCameraResolutionLevel()
	{
		CameraResolutionLevel level = CameraResolutionLevel.ResolutionMedium;

		try
		{
			IMediaDeviceInformation devInfo = mConferenceCore.getMediaDeviceInfo( DeviceType.Camera,
					mConferenceCore.getActiveVideoDevice());
			if( devInfo != null)
			{
				String res_level = devInfo.getProperty( ConferenceCore.RESOLUTION_LEVEL);
				if( !res_level.isEmpty())
					level = CameraResolutionLevel.valueOf( res_level);
			}
		} catch( DeviceNotSelectedException e)
		{
		}

		return level;
	}

	// Gets the available cameras
	public List<MediaDeviceWrapper> getCameras()
	{
		return GetDeviceList( DeviceType.Camera);
	}

	private List<MediaDeviceWrapper> GetDeviceList( DeviceType type)
	{
		List<MediaDeviceWrapper> devices = new ArrayList<MediaDeviceWrapper>();
		Vector<MediaDevice> mediaDevices = mConferenceCore == null ? null : mConferenceCore.getMediaDeviceList( type);
		if( mediaDevices != null)
		{
			for( MediaDevice mediaDevice : mediaDevices)
			{
				devices.add( new MediaDeviceWrapper( mediaDevice.getId(), mediaDevice.getDisplayName()));
			}
			Log.d( Utils.getOoVooTag(), "GetDeviceList: " + devices.toString());
		}
		return devices;
	}

	public String getErrorMessageForConferenceCoreError( ConferenceCoreError error)
	{
		if( mConferenceCore != null)
			return mConferenceCore.getErrorMessageForConferenceCoreError( error);
		else
			return null;
	}

	// Gets the available microphones
	public List<MediaDeviceWrapper> getMicrohones()
	{
		return GetDeviceList( DeviceType.Microphone);
	}

	public ParticipantsManager getParticipantsManager()
	{
		return mParticipantsManager;
	}

	public String getSDKVersion()
	{
		return mConferenceCore == null ? "" : mConferenceCore.getSDKVersion();
	}

	// Gets the available speakers
	public List<MediaDeviceWrapper> getSpeakers()
	{
		return GetDeviceList( DeviceType.Speaker);
	}

	private void handleWithDelay( Message message)
	{
		if( mDelayedMessages == null)
		{
			mDelayedMessages = new ArrayList<Message>();
		}
		Log.i( Utils.getOoVooTag(), "HANDLE WITH DELAY " + message.what);
		Message msg = new Message();
		msg.what = message.what;
		msg.obj = message.obj;
		mDelayedMessages.add( mDelayedMessages.size(), msg);
	}

	public boolean inCallMessagesPermitted()
	{
		return mConferenceCore.inCallMessagesPermitted();
	}

	public void initConference()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( INIT_CONFERENCE_CORE, 1000);
	}

	public void initConferenceCore()
	{
		String errStr = new String();

		if( mConferenceCore == null)
		{
			Log.i( TAG,
					"================================== Init ConferenceCore started ====================================");

			Log.i( TAG, " Register for internal events in ConferenceCore");
			// Register for internal events
			try
			{
				setCoreInstance( ConferenceCore.instance( mApp, this));
			} catch( CoreException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				errStr = "CPU NEON is NOT detected";
			}
		}

		Log.i( TAG, " Register for internal events in ConferenceCore");
		// Register for internal events
		UserSettings settings = retrieveSettings();

		ConferenceCoreError err = ConferenceCoreError.InvalidOperation;
		if( mConferenceCore != null)
		{
			err = mConferenceCore.initSdk( settings.AppId, settings.AppToken, settings.BaseURL);
			Log.d( TAG, "appId = " + settings.AppId + " token = " + settings.AppToken + " err = " + err);
			errStr = mConferenceCore.getErrorMessageForConferenceCoreError( err);
		}

		isSdkInitialize = err == ConferenceCoreError.OK;
		if( isSdkInitialize)
			Log.i( TAG, "Init	ConferenceCore finished");
		else
			Log.e( TAG, "Init ConferenceCore failed: (" + err + ") " + errStr);

		if( mSessionListenerList != null)
		{
			if( isSdkInitialize)
			{
				for( SessionListener listener : mSessionListenerList)
				{
					listener.onSessionInited();
				}
			}
			else
			{
				for( SessionListener listener : mSessionListenerList)
				{
					listener.onSessionError( errStr);
				}
			}
		}
	}

	public void initSession( ParticipantVideoSurface[] mParticipantsVideoSurfaces)
	{
		// Select devices
		try
		{
			int numOfVidOn = 0;
			for( Participant participant : mParticipantsManager.getParticipants())
			{
				if( numOfVidOn < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL)
				{
					Log.d( Utils.getOoVooTag(), "turning ParticipantVideoOn for " + participant.toString());
					if( participant.getId().isEmpty())
						continue;

					ConferenceCoreError err = doTurnParticipantVideoOn( participant.getId());
					Log.d( Utils.getOoVooTag(), "turning ParticipantVideoOn for " + participant.toString() + " rc = "
							+ err);

					if( ConferenceCoreError.OK == err)
					{
						Log.d( Utils.getOoVooTag(), "setting VideoStateOn for " + participant.toString());
						mParticipantsManager.setVideoOn( participant.getId(), true);
					}
				}
			}
		} catch( Exception e)
		{
			Log.e( TAG, "", e);
		}
	}

	public boolean isCameraMuted()
	{
		return mIsCameraMute;
	}

	public boolean isMicMuted()
	{
		return mIsMicrophoneMute;
	}

	public boolean isSdkInitialized()
	{
		return isSdkInitialize;
	}

	public boolean isSpeakerMuted()
	{
		return mAreSpeakersMute;
	}

	public boolean isVideoOn( int viewId)
	{
		return mParticipantsManager != null && mParticipantsManager.isVideoOn( viewId);
	}

	public boolean isVideoRenderActive( int viewId)
	{
		return mParticipantsManager != null && mParticipantsManager.isRenderActive( viewId);
	}

	public void joinSession()
	{
		Log.i(Utils.getOoVooTag(), "SENDING TO QUEUE JOIN_SESSION");

		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( JOIN_SESSION);
	}

	public void leaveSession()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( LEAVE_SESSION);
	}

	public void loadDataFromSettings()
	{
		try
		{
			UserSettings settings = retrieveSettings();
			((ConferenceCore) mConferenceCore).setLogListener( this, settings.CurrentLogLevel);

			if( mConferenceCore == null)
			{
				LogSdk.d( TAG, "set configuration before initialization");
				return;
			}

			mConferenceCore.selectCamera( CameraState.toInt(settings.CameraMuted ? CameraState.MUTE_CAMERA : CameraState.FRONT_CAMERA));
			mConferenceCore.setCameraResolutionLevel( CameraResolutionLevel.ResolutionMedium);
		} catch( Exception e)
		{
			LogSdk.e( TAG, "An Error occured while trying to select Devices");
		}
	}

	public String myId()
	{
		return mMySessionId;
	}

	@Override
	public void OnCameraTurnedOff( ConferenceCoreError errorCode)
	{
		LogSdk.d( TAG, "OnCameraTurneedOff rc = " + errorCode);

		doTurnParticipantVideoOff( mMySessionId, mMySessionId);

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}
		LogSdk.d(TAG, "Camera turned off. " + errorCode);
	}

	@Override
	public void OnCameraTurnedOn( ConferenceCoreError errorCode, FrameSize frameSize, int fps)
	{
		Log.d( Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode + " muted = "
				+ isCameraMuted());

		doTurnParticipantVideoOn(mMySessionId);

		if( mSessionControlsListenerList == null || mSessionControlsListenerList.size() == 0)
			Log.w( Utils.getOoVooTag(), "No one listen OnCameraTurnedOn event..");

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onCameraOn();
			}
		}
	}

	@Override
	public void OnConferenceError( ConferenceCoreError errorCode)
	{
		Log.d( Utils.getOoVooTag(), "OnSessionError - recieved error:" + errorCode);
		if( mSessionListenerList != null)
		{
			for( SessionListener listener : mSessionListenerList)
			{
				listener.onSessionError( mConferenceCore.getErrorMessageForConferenceCoreError( errorCode));
			}
		}
		LogSdk.d( TAG, "Session Error: " + errorCode);
	}

	private void onConferenceEvent( Message msg)
	{
		try
		{
			int command = msg.what;
			Log.i(Utils.getOoVooTag(), "onConferenceEvent :: " + command);
			switch( command)
			{
			case JOIN_SESSION:
				doJoinSession();
				break;
			case LEAVE_SESSION:
				doLeaveSession();
				break;
			case END_SESSION:
				doEndOfCall();
				break;
			case MUTE_CAMERA:
				setCameraMuted( !mIsCameraMute, true);
				break;
			case SWITCH_CAMERA:
				switchCamera();
				break;
			case MUTE_MIC:
				setMicrophoneMuted( !mIsMicrophoneMute);
				break;
			case MUTE_SPEAKERS:
				setSpeakersMuted( !mAreSpeakersMute);
				break;
			case VIDEO_RESOLUTION:
				if( msg.obj != null && msg.obj instanceof CameraResolutionLevel)
				{
					doSetVideoResolution( (CameraResolutionLevel) msg.obj);
				}
				break;
			case SELECT_CAMERA:
				if( msg.obj != null && msg.obj instanceof Integer)
				{
					Integer cameraId = (Integer) msg.obj;
					doSelectCamera( cameraId.intValue());
				}
				break;
			case TURN_ON_PARTICIPANT_VIDEO:
				if( msg.obj != null && msg.obj instanceof String)
				{
					doTurnParticipantVideoOn( (String) msg.obj);
				}
				break;
			case TURN_OFF_PARTICIPANT_VIDEO:
				if( msg.obj != null && msg.obj instanceof String[])
				{
					String[] data = (String[]) msg.obj;
					doTurnParticipantVideoOff( data[0], data[1]);
				}
				break;
			case INIT_CONFERENCE_CORE:
				initConferenceCore();
				break;
			case UI_SWITCH_CONFERENCE_MODE:
				if( msg.obj != null && msg.obj instanceof Integer)
				{
					Integer data = (Integer) msg.obj;
					doSwitchUIFullMode( data.intValue());
				}
				break;
			case UI_READY:
				Boolean ready = (Boolean) msg.obj;
				isUIReadyState = ready.booleanValue();
				if( isUIReadyState)
				{

					if( mDelayedMessages != null && !mDelayedMessages.isEmpty())
					{
						Log.i( Utils.getOoVooTag(),
								"UI is ready :: continue with delayed messages " + mDelayedMessages.size());
						for( int i = 0; i < mDelayedMessages.size(); i++)
						{
							Message message = mDelayedMessages.get( i);
							Log.i( Utils.getOoVooTag(), "\t\tDelayed message = " + message.what);
							if( message.what == UI_PREPARE_HOLDER_USER)
							{
								if( message.obj != null && message.obj instanceof String[])
								{
									String[] data = (String[]) message.obj;
									prepareParticipantActiveRender( data[0], data[1]);
								}
							}
						}
						mDelayedMessages.clear();
						mDelayedMessages = null;
					}
				}
				break;
			case UI_PREPARE_HOLDER_USER:
				Log.i( Utils.getOoVooTag(), "UI_PREPARE_HOLDER_USER event :: isUIReadyState = " + isUIReadyState);

				if( !isUIReadyState)
					handleWithDelay( msg);
				else
				{
					if( msg.obj != null && msg.obj instanceof String[])
					{
						String[] data = (String[]) msg.obj;
						prepareParticipantActiveRender( data[0], data[1]);
					}
				}
				break;

			}
		} catch( Exception e)
		{
			Log.e( TAG, "", e);
		}
	}

	@Override
	public void OnConnectionStatisticsUpdate( ConnectionStatistics connectionStatistics)
	{
		DecimalFormat df = new DecimalFormat( "#.##");
		Log.d( Utils.getOoVooTag(),
				"Connection Statistics Update. InboundBandwidth=" + connectionStatistics.InboundBandwidth * 8 / 1024
				+ "Kbps InboundPacketLoss=" + df.format( connectionStatistics.InboundPacketLoss)
				+ "% OutboundPacketLoss=" + connectionStatistics.OutboundPacketLoss + "%");
		LogSdk.d( TAG, "Connection Statistics Update. InboundBandwidth=" + connectionStatistics.InboundBandwidth * 8
				/ 1024 + "Kbps InboundPacketLoss=" + connectionStatistics.InboundPacketLoss + "% OutboundPacketLoss="
				+ connectionStatistics.OutboundPacketLoss);
	}

	@Override
	public void OnGetMediaDeviceList( MediaDevice[] aDeviceArray)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Fired on Incoming phone Call ON/OFF
	 *
	 * @param reason
	 */
	@Override
	public void OnHold( String reason)
	{
		LogSdk.d( TAG, "On hold by reason: " + reason);
	}

	@Override
	public void OnIncallMessage( byte[] buffer, String participantId)
	{
		LogSdk.d( TAG, "Message from: " + participantId + " - " + buffer);

	}

	@Override
	public void OnJoinConference( ConferenceCoreError errorCode, String mySessionId)
	{
		if( errorCode == ConferenceCoreError.OK)
		{
			mParticipantsManager.onParticipantLeftSession( mMySessionId); // remove old my Id
			mMySessionId = mySessionId;
			mParticipantsManager.onParticipantJoinedSession( mMySessionId, "Me", -1);

			LogSdk.d( TAG, "Joined Session myId = " + mMySessionId + " error = " + errorCode);
			doTurnParticipantVideoOn( mySessionId);

			setMicrophoneMuted( false);
			setSpeakersMuted( false);
		}

		if( mSessionListenerList != null)
		{
			for( SessionListener listener : mSessionListenerList)
			{
				if( errorCode == ConferenceCoreError.OK)
					listener.onJoinSessionSucceeded();
				else
					listener.onJoinSessionError( errorCode);
			}
			LogSdk.d( TAG, "Failed to join: " + errorCode);
		}

		LogSdk.d( TAG, "Joined Session myId = " + mySessionId + "errorCode = " + errorCode);
	}

	@Override
	public void OnLeftConference( ConferenceCoreError errorCode)
	{
		mParticipantsManager.onParticipantLeftSession( mMySessionId);
		mParticipantsManager.onLeftSession( errorCode);

		if( mSessionListenerList != null)
		{
			for( SessionListener listener : mSessionListenerList)
			{
				listener.onLeftSession( errorCode);
			}
		}

		mMySessionId = ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT;
		mParticipantsManager.onParticipantJoinedSession(mMySessionId, "Me", -1);

		LogSdk.d(TAG, "Left Session " + errorCode);
	}

	@Override
	public void OnLog( LogLevel level, String tag, String message)
	{
		mFileLogger.OnLog(level, tag, message);
	}

	@Override
	public void OnParticipantJoinedConference( String sParticipantId, String sOpaqueString)
	{
		try
		{
			Log.i( Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - adding participant to holder "
					+ sOpaqueString + "(" + sParticipantId + ") -> started");
			mParticipantsManager.onParticipantJoinedSession( sParticipantId, sOpaqueString, -1);

			Log.d( Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - there are "
					+ mParticipantsManager.getNoOfVideosOn() + "/"
					+ ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL + " participants in conference already");
			if( mParticipantsManager.getNoOfVideosOn() <= ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL)
			{
				turnParticipantVideoOn( sParticipantId);
			}
			else
				Log.d( Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - num of videos "
						+ mParticipantsManager.getNoOfVideosOn() + "/"
						+ ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL);
		} catch( Exception ex)
		{
			Log.e( Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - An Exception:", ex);
		}
		finally
		{
			if( mConferenceQueue != null)
				mConferenceQueue.sendMessage( UI_PREPARE_HOLDER_USER, new String[] { sParticipantId, sOpaqueString });

			int participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);

			if( mSessionParticipantsListenerList != null)
			{
				for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
				{
					listener.onParticipantJoinedSession( sParticipantId, participantViewId, sOpaqueString);
				}
			}

			LogSdk.d(TAG, "joinSession for user: " + sParticipantId + " With Display Name: " + sOpaqueString);
		}
	}

	@Override
	public void OnParticipantLeftConference( String sParticipantId)
	{
		int participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);

		String displayName = "";
		Participant participant = mParticipantsManager.getParticipant( sParticipantId);
		if( participant != null)
			displayName = participant.getDisplayName();
		LogSdk.d( TAG, "Participant: (" + displayName + ") Left the session");

		boolean isUpdateFullMode = mParticipantsManager.onParticipantLeftSession( sParticipantId);
		turnParticipantVideoOff( sParticipantId, displayName);

		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantLeftSession( participantViewId, sParticipantId);
			}
		}
		if( isUpdateFullMode)
		{
			mSessionUIPresenter.onMultiModeChanged();
		}
	}

	@Override
	public void OnParticipantVideoPaused( String sParticipantId)
	{
		LogSdk.d( TAG, sParticipantId + " video Paused");
		int participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);
		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoPaused( participantViewId);
			}
		}
	}

	@Override
	public void OnParticipantVideoReceiveOff( ConferenceCoreError errorCode, String sParticipantId)
	{
		LogSdk.d( TAG, sParticipantId + " turned video Off " + errorCode);
		int participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);
		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoTurnedOff( errorCode, participantViewId, sParticipantId);
			}
		}
		mParticipantsManager.turnVideoOff( sParticipantId);

		String displayName = "";
		Participant participant = mParticipantsManager.getParticipant( sParticipantId);
		if( participant != null)
			displayName = participant.getDisplayName();
		LogSdk.d(TAG, "Participant: (" + displayName + ") Video Turned Off " + errorCode);
	}

	@Override
	public void OnParticipantVideoReceiveOn( ConferenceCoreError errorCode, String sParticipantId, FrameSize frameSize)
	{
		int participantViewId = -1;
		String displayName = "";

		try
		{
			Log.i( Utils.getOoVooTag(), "ConferenceManager.OnParticipantVideoReceiveOn : " + sParticipantId + "; "
					+ frameSize.toString() + " {" + errorCode + "}");

			if( mParticipantsManager.turnVideoOn( sParticipantId, false))
			{
				LogSdk.d( TAG, sParticipantId + " turned video On " + errorCode);
				participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);
				Participant participant = mParticipantsManager.getParticipant( sParticipantId);

				if( participant != null)
					displayName = participant.getDisplayName();
			}
			else
			{
				LogSdk.d( TAG, sParticipantId + " turned video Off " + errorCode);
				errorCode = ConferenceCoreError.InvalidPointer;
			}

		} catch( Exception ex)
		{
			Log.e( Utils.getOoVooTag(), "ConferenceManager.OnParticipantVideoReceiveOn - an Exception occured:", ex);
			errorCode = ConferenceCoreError.Error;
		}

		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoTurnedOn(errorCode, sParticipantId, frameSize, participantViewId,
						displayName);
			}
		}

		LogSdk.d(TAG, "Participant (" + displayName + ") Video Turned On. Video format: width=" + frameSize.Width
				+ " height=" + frameSize.Height + " errorCode = " + errorCode);
	}

	@Override
	public void OnParticipantVideoResumed( String sParticipantId)
	{
		LogSdk.d( TAG, sParticipantId + " video Resumed");
		int participantViewId = mParticipantsManager.getViewIdByParticipant( sParticipantId);

		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoResumed(participantViewId, sParticipantId);
			}
		}
	}

	@Override
	public void onPause()
	{
		Log.d(Utils.getOoVooTag(), "ConferenceManager - onPause");
		if( mParticipantsManager != null)
			mParticipantsManager.Pause();
		boolean muted = isCameraMuted();
		turnCameraOn( false);
		mIsCameraMute = muted;
		LogSdk.d(TAG, "onPause. camera muted " + mIsCameraMute);
	}

	@Override
	public void OnPreviewTurnedOff( ConferenceCoreError errorCode)
	{
		Log.d(Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode + " CameraMuted = "
				+ mIsCameraMute);

		if( errorCode == ConferenceCoreError.OK)
			if( !mMySessionId.equals( LOCAL_PARTICIPANT_ID_DEFAULT))
				setCameraTransmit( false);

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}
	}

	@Override
	public void OnPreviewTurnedOn( ConferenceCoreError errorCode)
	{
		LogSdk.d( TAG, "OnPreviewTurnedOn rc = " + errorCode);

		if( errorCode == ConferenceCoreError.OK)
		{
			if( mParticipantsManager.turnVideoOn( mMySessionId, true))
				Log.d( TAG, "OnMyVideoTurnedOn: video is ON for " + mMySessionId);

			if( !mMySessionId.equals( LOCAL_PARTICIPANT_ID_DEFAULT))
				setCameraTransmit( true);
		}

		Log.d(Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode + " CameraMuted = "
				+ mIsCameraMute);

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}

		LogSdk.d( TAG, "My preview turned on. " + errorCode);
	}

	@Override
	public void onResume()
	{
		LogSdk.d( TAG, "onResume");
		mParticipantsManager.Resume();

		if( mSessionUIPresenter != null)
			mSessionUIPresenter.initSurfaces();

		Log.d( Utils.getOoVooTag(), "ParticipantsManager.getInstance().getParticipants().size() = "
				+ mParticipantsManager.getParticipants().size());
		for( Participant participant : mParticipantsManager.getParticipants())
		{
			int participantViewId = mParticipantsManager.getViewIdByParticipant( participant.getId());
			Log.d( Utils.getOoVooTag(), "onResume - participantViewId =" + participantViewId + " participantId "
					+ participant.getId() + " video is on " + mParticipantsManager.isVideoOn( participant.getId()));
			if( participantViewId != -1)
			{
				if( mSessionUIPresenter != null)
					mSessionUIPresenter.updateParticipantSurface( participantViewId, participant.getDisplayName(),
							participant.getIsVideoOn());
			}
		}

		CameraResolutionLevel level = getCameraResolutionLevel();
		setVideoResolution( level);
		turnCameraOn( true);
	}

	@Override
	public void OnUnHold( String reason)
	{
		LogSdk.d(TAG, "On hold done. reason: " + reason);
	}

	@Override
	public void OnVideoTransmitTurnedOff( ConferenceCoreError errorCode)
	{
		Log.d(Utils.getOoVooTag(), "My video transmit turned off error = " + errorCode);
	}

	@Override
	public void OnVideoTransmitTurnedOn( ConferenceCoreError errorCode)
	{
		Log.d(Utils.getOoVooTag(), "My video transmit turned on error = " + errorCode);
	}

	public void pauseSession()
	{
		Log.d( Utils.getOoVooTag(), "Pause event from UI - pauseSession");
		try
		{
			mConferenceCore.pause();
		} catch( Exception e)
		{
			Log.e( TAG, "", e);
		}
	}

	// Commit the settings to the shared preferences file
	public void persistSettings( UserSettings toPersist)
	{
		mSettingsManager.persistSettings(toPersist);
	}

	private void prepareParticipantActiveRender( String sParticipantId, String sOpaqueString)
	{
		int participantViewId = -1;
		Log.i( Utils.getOoVooTag(), "ConferenceManager.prepareParticipantActiveRender - adding participant to holder "
				+ sOpaqueString + " (" + sParticipantId + ") -> started");
		participantViewId = mParticipantsManager.prepareParticipantAsActiveRender( sParticipantId);

		Log.i( Utils.getOoVooTag(), "ConferenceManager.prepareParticipantActiveRender - adding participant to holder "
				+ sOpaqueString + " (" + participantViewId + ") <- finished");

		if( mSessionParticipantsListenerList != null)
		{
			for( SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantJoinedSession( sParticipantId, participantViewId, sOpaqueString);
			}
		}
	}

	public void removeSessionControlsListener( SessionControlsListener listener)
	{
		if( mSessionControlsListenerList != null)
			mSessionControlsListenerList.remove( listener);
	}

	public void removeSessionListener( SessionListener l)
	{
		if( mSessionListenerList != null)
			mSessionListenerList.remove( l);
	}

	public void removeSessionParticipantsListener( SessionParticipantsListener l)
	{
		if( mSessionParticipantsListenerList != null)
			mSessionParticipantsListenerList.remove( l);
	}

	public void resetFlagSdkInited()
	{
		isSdkInitialize = false;
		Log.i( TAG, "Reset InitSdk done.");
	}

	public void resumePreviewSession()
	{
		try
		{
			Log.d( Utils.getOoVooTag(), "setting preview video surface");
			SurfaceView view = createSurfaceView();
			if( view == null)
				Log.e( Utils.getOoVooTag(), "Surface is null");
			else
				Log.i( Utils.getOoVooTag(), "Surface is not null");
			mConferenceCore.setPreviewSurface( view);
			mConferenceCore.resume();
		} catch( Exception e)
		{
			Log.e( TAG, "resumePreviewSession Error:", e);
		}
	}

	public void updateMicrophoneState()
	{
		MediaDevice dev = mConferenceCore.getMediaDevice( DeviceType.Microphone, -1);
		if( dev != null)
		{
			Log.d( Utils.getOoVooTag(), "Microphone info: " + dev.toString());
			setMicrophoneMuted(dev.State != DeviceState.ON);
		}
	}

	public void resumeSession()
	{
		Log.d( Utils.getOoVooTag(), "Resume event from UI - resumeSession");
		try
		{
			Log.d( Utils.getOoVooTag(), "setting preview video surface");
			SurfaceView view = createSurfaceView();
			if( view == null)
				Log.e( Utils.getOoVooTag(), "Surface is null");
			else
				Log.i( Utils.getOoVooTag(), "Surface is not null");
			mConferenceCore.setPreviewSurface( view);
			mConferenceCore.resume();

			MediaDevice dev = mConferenceCore.getMediaDevice( DeviceType.Microphone, -1);
			if( dev != null)
			{
				Log.d( Utils.getOoVooTag(), "Microphone info: " + dev.toString());
				setMicrophoneMuted( dev.State != DeviceState.ON);
			}

			dev = mConferenceCore.getMediaDevice( DeviceType.Speaker, -1);
			if( dev != null)
			{
				Log.d( Utils.getOoVooTag(), "Speaker info: " + dev.toString());
				setSpeakersMuted( dev.State != DeviceState.ON);
			}

			dev = mConferenceCore.getMediaDevice( DeviceType.Camera, -1);
			if( dev != null)
			{
				Log.d( Utils.getOoVooTag(), "Camera info: " + dev.toString());
			}
		} catch( Exception e)
		{
			Log.e( TAG, "resumeSession Error:", e);
		}
	}

	// Retrieve the settings from the shared preferences file
	public UserSettings retrieveSettings()
	{
		return mSettingsManager.retrieveSettings();
	}

	public void setActiveFilter( String id)
	{
		LogSdk.d( TAG, "Set Active Filter to " + id);
		if( isUIReadyState)
		{
			mConferenceCore.setActiveVideoFilter( id);
		}
	}

	public void setCameraMuted( final boolean muted, final boolean change_mute_status)
	{
		if( change_mute_status)
			mIsCameraMute = muted;

		Log.d( Utils.getOoVooTag(), "Setting camera mute to: " + muted + ", mIsCameraMute: " + mIsCameraMute
				+ ", MyId: " + mMySessionId);
		if( !muted)
		{
			Log.d( Utils.getOoVooTag(), "Setting camera preview to: " + (muted ? "OFF" : "ON"));
			mConferenceCore.turnPreviewOn();
		}
		else
		{
			Log.d( Utils.getOoVooTag(), "Setting camera preview to: " + (muted ? "OFF" : "ON"));
			mConferenceCore.turnPreviewOff();
		}
	}

	public void setCameraTransmit( final boolean on)
	{
		Log.d( Utils.getOoVooTag(), "Setting camera transmit to: " + (on ? "OFF" : "ON") + ", MyId " + mMySessionId);

		if( on)
			mConferenceCore.turnVideoTransmitOn();
		else
			mConferenceCore.turnVideoTransmitOff();
	}

	@Override
	public void setCoreInstance( IConferenceCore core_instance)
	{
		mConferenceCore = core_instance;
	}

	private void setMicrophoneMuted( boolean shouldMute)
	{
		Log.d( Utils.getOoVooTag(), "Setting microphone mute to: " + shouldMute);
		mIsMicrophoneMute = shouldMute;
		mConferenceCore.setRecorderMuted( shouldMute);

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetMicrophoneEnabled( true);
				listener.onSetMicrophoneMuted( shouldMute);
			}
		}
	}

	public void setSessionUIPresenter( SessionUIPresenter presenter)
	{
		mSessionUIPresenter = presenter;
	}

	private void setSpeakersMuted( boolean shouldMute)
	{
		Log.d( Utils.getOoVooTag(), "Setting speaker mute to: " + shouldMute);
		mAreSpeakersMute = shouldMute;
		mConferenceCore.setPlaybackMuted(shouldMute);

		if( mSessionControlsListenerList != null)
		{
			for( SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetSpeakersEnabled( true);
				listener.onSetSpeakersMuted( shouldMute);
			}
		}
	}

	public void setUIReadyState( boolean ready)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( UI_READY, Boolean.valueOf( ready));
	}

	public void setVideoResolution( CameraResolutionLevel resolution)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( VIDEO_RESOLUTION, resolution);
	}

	public void selectCamera( int cameraId)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( SELECT_CAMERA, Integer.valueOf( cameraId));
	}

	public void switchCamera()
	{
		try
		{
			int cameraId = mConferenceCore.getActiveVideoDevice();
			CameraResolutionLevel level = getCameraResolutionLevel();

			for( MediaDeviceWrapper camera : getCameras())
			{
				if( cameraId != camera.getDeviceId())
				{
					cameraId = camera.getDeviceId();
					mConferenceCore.selectCamera( cameraId);
					mConferenceCore.setCameraResolutionLevel( level);
					break;
				}
			}

		} catch( Exception e)
		{
			Log.e( Utils.getOoVooTag(), "An Exception thrown while calling switchCamera ", e);
		}
	}

	public void switchUIFullMode( int id)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( UI_SWITCH_CONFERENCE_MODE, Integer.valueOf( id));
	}

	// Start toggle camera mute logic
	public void toggleCameraMute()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( MUTE_CAMERA);
	}

	// Start toggle camera switch logic
	public void toggleCameraSwitch()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( SWITCH_CAMERA);
	}

	// Start toggle microphone mute logic
	public void toggleMicrophoneMute()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( MUTE_MIC);
	}

	// Start toggle speakers mute logic
	public void toggleSpeakersMute()
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( MUTE_SPEAKERS);
	}

	public void turnCameraOn( boolean on)
	{
		// boolean skipTurn = false;
		int cameraId = -1;
		try
		{
			cameraId = mConferenceCore.getActiveVideoDevice();
		} catch( DeviceNotSelectedException e)
		{
			try
			{
				mConferenceCore.selectCamera( CameraState.toInt( CameraState.FRONT_CAMERA));
			} catch( Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		Log.d( Utils.getOoVooTag(), "Turn camera " + cameraId + " to: " + on);
		if( on)
		{
			mConferenceCore.turnCameraOn();
		}
		else
		{
			mConferenceCore.turnCameraOff();
		}
	}

	public void turnParticipantVideoOff( String id, String displayName)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( TURN_OFF_PARTICIPANT_VIDEO, new String[] { id, displayName });
	}

	public void turnParticipantVideoOn( String id)
	{
		if( mConferenceQueue != null)
			mConferenceQueue.sendMessage( TURN_ON_PARTICIPANT_VIDEO, id);
	}

	public void updateGLView( int id, SessionUIPresenter activity)
	{
		mParticipantsManager.updateGLView(id, activity, mConferenceCore, myId());

	}

	public int getViewIdByParticipant( String id)
	{
		return mParticipantsManager.getViewIdByParticipant( id);
	}
}
