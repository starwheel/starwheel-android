//
// ParticipantHolder.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo.Common;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.ooVoo.SessionUIPresenter;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.Exceptions.CoreException;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.Utils.MethodUtils;
import com.oovoo.core.ui.ooVooSdkGLSurfaceView;

public class ParticipantHolder
{

	public class RenderViewData
	{
		private int						_view_id;
		private boolean					_video_on;
		private boolean					_isFullMode		= false;
		private boolean					_isPreview;
		private ooVooSdkGLSurfaceView	_view;
		private VideoParticipant		_participant	= null;

		public RenderViewData(int view_id, boolean isPreview)
		{
			_view_id = view_id;
			_video_on = false;
			_isPreview = isPreview;
			_view = null;
		}

		@Override
		public String toString()
		{
			return "id: " + _view_id + " isOn: " + _video_on + " isPreview: " + _isPreview + " isFullMode: "
					+ _isFullMode + " GLView: " + (_view == null ? "NULL" : _view.toString()) + " participant: "
					+ (_participant != null ? _participant.toString() : "FREE!");
		}

		public ooVooSdkGLSurfaceView getView()
		{
			return _view;
		}

		public boolean isFullMode()
		{
			return _isFullMode;
		}

		public boolean isPreview()
		{
			return _isPreview;
		}

		public boolean isVideoOn()
		{
			return _video_on;
		}

		public void setVideoOn( boolean on)
		{
			_video_on = on;
		}

		public void setView( ooVooSdkGLSurfaceView _view)
		{
			this._view = _view;
			_isPreview = _view.isPreview();
		}

		public void setParticipant( VideoParticipant p)
		{
			if( _participant != null && p != null && !p.getParticipantId().equals( _participant.getParticipantId()))
				LogSdk.e( TAG, prefix() + "participant: " + _participant + " is NOT NULL when trying to set: " + p);
			_participant = p;
		}

		public boolean isConnected()
		{
			return _view.isConnected();
		}

		public void disconnect()
		{
			_isFullMode = false;
			if( _view != null)
				_view.disconnect();
			_video_on = false;
		}
	}

	public class VideoParticipant
	{
		private String	_participantId;
		private String	_opaqueString;
		private int		_viewId	= -1;

		public VideoParticipant(String sParticipantId, String sOpaqueString, int viewId)
		{
			setOpaqueString( sOpaqueString);
			setViewId( viewId);
			setParticipantId( sParticipantId);
		}

		public String getOpaqueString()
		{
			return _opaqueString;
		}

		public String getParticipantId()
		{
			return _participantId;
		}

		public int getViewId()
		{
			return _viewId;
		}

		public void setOpaqueString( String opaqueString)
		{
			this._opaqueString = opaqueString;
		}

		public void setParticipantId( String participantId)
		{
			this._participantId = participantId;
		}

		public void setViewId( int viewId)
		{
			this._viewId = viewId;
		}

		@Override
		public String toString()
		{
			return _opaqueString + " (" + _participantId + ") on viewId = " + _viewId;
		}
	}

	private static final String				TAG				= ParticipantHolder.class.getSimpleName();
	private SparseArray<RenderViewData>		_renders		= new SparseArray<RenderViewData>();
	private SparseArray<VideoParticipant>	_users			= new SparseArray<VideoParticipant>();
	private boolean							_on_pause		= true;
	private boolean							_on_full_mode	= false;
	private int								_viewIdForFullMode;

	public String toString( String prefix)
	{
		String s = new String();
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData data = _renders.valueAt( i);
			s += prefix + " [" + i + "] " + data.toString() + "\n";
		}
		return s;
	}

	public void addParticipant( String sParticipantId, String sOpaqueString, int view_id)
	{
		VideoParticipant p = _users.get( sParticipantId.hashCode());
		if( p != null && p.getParticipantId().equals( sParticipantId))
		{
			Log.w( TAG, prefix() + "add Participant failed! Participant <" + sParticipantId + "> already exist. " + p);
			return;
		}

		_users.put( sParticipantId.hashCode(), new VideoParticipant( sParticipantId, sOpaqueString, view_id));
		Log.d( TAG, prefix() + "add participant: <" + sParticipantId + "> total: " + _users.size());
	}

	public void removeParticipants()
	{
		_users.clear();
	}

	public void clear()
	{
		this._renders.clear();
		_on_full_mode = false;
		_on_pause = false;
		Log.d( TAG, prefix() + "clear all");
	}

	private void fillEmptyRenders()
	{
		for( int i = 0; i < _users.size(); i++)
		{
			VideoParticipant participant = _users.valueAt( i);
			RenderViewData d = getFreeRender(participant.getParticipantId());
			if( d != null && participant.getViewId() == -1)
			{
				participant.setViewId( d._view_id);
				d.setParticipant( participant);
				Log.i( TAG, prefix() + "Add free render for " + participant + " as " + d);
				d._video_on = true;
				try
				{
					ConferenceCore.instance().receiveParticipantVideoOn( participant.getParticipantId());
				} catch( CoreException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public String findRenderIdByViewId( int view_id)
	{

		for( int i = 0; i < _renders.size(); i++)
		{
			int key = _renders.keyAt( i);
			if( key == view_id)
			{
				RenderViewData data = _renders.valueAt( i);
				return data._view.getParticipantId();
			}
		}
		return null;
	}

	public int getActiveRenders()
	{
		int rc = 0;
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			if( !TextUtils.isEmpty( d._view.getParticipantId()))
				rc++;
		}
		return rc;
	}

	private RenderViewData getFreeRender( String participantId)
	{
		RenderViewData rd = null;
		Log.d( Utils.getOoVooTag(), prefix() + "RENDERS Size = " + _renders.size() + " current views: \n"
				+ toString( "getFreeRender"));
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);

			if( d._participant == null || d._participant.getParticipantId().equals( participantId))
			{
				rd = d;
				Log.i( Utils.getOoVooTag(), prefix() + "found free render: " + rd.toString());
				break;
			}
		}
		return rd;
	}

	public int getNumOfVideosOn()
	{
		int rc = 0;
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			if( d.isVideoOn())
				rc++;
		}
		return rc;
	}

	public VideoParticipant getParticipant( String sParticiapntId)
	{
		return _users.get( sParticiapntId.hashCode());
	}

	public String getParticipantByViewId( int view_id)
	{
		String user = null;
		for( int i = 0; i < _users.size(); i++)
		{
			VideoParticipant p = _users.valueAt( i);
			if( p.getViewId() == view_id)
			{
				user = p.getParticipantId();
				break;
			}
		}
		return user;
	}

	public SparseArray<VideoParticipant> getParticipants()
	{
		return _users;
	}

	public int getViewIdByParticipant( String sParticipantId)
	{
		VideoParticipant p = _users.get( sParticipantId.hashCode());
		if( p == null)
		{
			return -1;
		}

		return p.getViewId();
	}

	public int getViewIdForFullMode()
	{
		return _viewIdForFullMode;
	}

	public boolean isFullMode()
	{
		return _on_full_mode;
	}

	public Boolean isFullMode( String participant)
	{
		VideoParticipant vp = _users.get( participant.hashCode());
		if( vp == null)
		{
			Log.w( TAG, "Participant " + participant + " not found!");
			return false;
		}
		Integer view_id = vp.getViewId();
		if( view_id != null && view_id != -1)
		{
			return _renders.get( view_id).isFullMode();
		}
		return false;
	}

	public boolean isRenderActive( int viewId)
	{
		RenderViewData mRenderViewData = _renders.get( viewId);
		if( mRenderViewData != null && mRenderViewData._view.getParticipantId() != null
				&& mRenderViewData._view.isConnected())
		{
			return true;
		}

		return false;
	}

	public boolean isVideoOn( int viewId)
	{
		RenderViewData d = _renders.get( viewId);
		if( viewId != -1 && d != null)
		{
			return _renders.get( viewId).isVideoOn();
		}
		else
			Log.w( TAG, prefix() + "view not found for viewId=" + viewId);
		return false;
	}

	public boolean isVideoOn( String participant)
	{
		VideoParticipant vp = _users.get( participant.hashCode());
		if( vp == null)
		{
			Log.w( TAG, prefix() + "Participant " + participant + " not found! video is OFF returned");
			return false;
		}

		int view_id = vp.getViewId();
		RenderViewData d = _renders.get( view_id);
		if( view_id != -1 && d != null)
		{
			Log.d( TAG, prefix() + "video is " + d.isVideoOn() + " for participant: " + vp.toString());
			return d.isVideoOn();
		}
		else
			Log.d( TAG, prefix() + "video is OFF for participant: " + participant);
		return false;
	}

	public void moveToFullMode( int viewIdForFullMode)
	{
		_on_full_mode = true;
		_viewIdForFullMode = viewIdForFullMode;
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			if( d._view_id != viewIdForFullMode)
			{
				String user = getParticipantByViewId( d._view_id);
				Log.d( TAG, "pausing GLview " + d._view_id + " for user " + user);
				d._isFullMode = false;
			}
			else
			{
				d._isFullMode = true;
			}
		}
	}

	public void moveToMultiMode( int viewIdForFullMode)
	{
		_on_full_mode = false;
		int numActiveRenders = 0;
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			d._isFullMode = false;

			if( d._view_id != viewIdForFullMode)
			{
				if( d._video_on && d._view.getParticipantId() != null)
				{
					numActiveRenders++;
				}
			}
			else
			{
				numActiveRenders++;
			}
		}

		if( numActiveRenders < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL && _users.size() >= numActiveRenders)
		{
			fillEmptyRenders();
		}
	}

	public void Pause()
	{
		Log.d( TAG, prefix() + "Pause: on_pause=" + _on_pause);
		_on_pause = true;

		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			String user = getParticipantByViewId(d._view_id);

			if( user != null)
			{
				Log.d( TAG, prefix() + "pausing GLview " + d._view_id + " for user " + user);
				if( d._video_on)
				{
					try
					{
						ConferenceCore.instance().receiveParticipantVideoOff( user);
					} catch( CoreException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d( TAG, prefix() + "send turn video Off for user " + user);
				}
			}
		}
	}

	protected String prefix()
	{
		return MethodUtils.getShortCallingMethodNameFromStack( 2) + " -> " + MethodUtils.getShortCallingMethodName()
				+ ": ";
	}

	public int prepareParticipantAsActiveRender( String sParticipantId)
	{
		int view_id = -1;
		int numActiveRenders = getActiveRenders();

		Log.d( Utils.getOoVooTag(), prefix() + "Active renders = " + numActiveRenders);

		if( numActiveRenders < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL)
		{
			Log.d( Utils.getOoVooTag(), prefix() + "Add joined user to active renders " + sParticipantId);

			RenderViewData d = getFreeRender( sParticipantId);
			if( d != null)
			{
				VideoParticipant p = _users.get( sParticipantId.hashCode());
				if( p != null)
				{
					p.setViewId( d._view_id);
					d.setParticipant( p);
					Log.d( Utils.getOoVooTag(), prefix() + "Add free render for " + p + " as " + d._view_id);
				}
				else
				{
					Log.e( TAG, prefix() + "Participant " + sParticipantId + " NOT FOUND");
				}
				d._video_on = true;
				view_id = d._view_id;
			}
			else
			{
				Log.w( Utils.getOoVooTag(), prefix() + "No available GLView found for Participant " + sParticipantId);
			}
		}
		else
		{
			Log.w( Utils.getOoVooTag(), prefix()
					+ "ConferenceManager.OnParticipantJoinedSession - can't add user to active surfaces");
		}

		// if( view_id != -1)
		// {
		// Log.d( TAG, prefix() + "turning video on for " + sParticipantId);
		// if( !isFullMode())
		// {
		// setVideoOn( sParticipantId, true);
		// Log.d( TAG, prefix() + "participantViewId = " + view_id);
		// }
		// }
		return view_id;
	}

	public boolean removeParticipant( String sParticipantId)
	{
		boolean updateFullMode = false;
		VideoParticipant p = _users.get( sParticipantId.hashCode());
		if( p == null)
		{
			Log.e( TAG, prefix() + "remove Participant failed! Participant " + sParticipantId + " not found");
			return updateFullMode;
		}

		int view_id = p.getViewId();
		if( view_id == -1)
		{
			Log.d( TAG, prefix() + "Participant " + sParticipantId + " is not connected to any GLView");
			RenderViewData d = getRenderByParticipantId( p.getParticipantId());
			if( d != null)
			{
				d.disconnect();
				d.setParticipant( null);
			}
		}
		else
		{
			RenderViewData d = _renders.get( view_id);
			if( d != null)
			{
				updateFullMode = d._isFullMode;
				d.disconnect();
				d.setParticipant( null);
			}
		}
		_users.remove( sParticipantId.hashCode());

		if( updateFullMode)
			moveToMultiMode( -1);

		Log.d( TAG, prefix() + "remove Participant " + sParticipantId + " GLView " + view_id + " is free. total: "
				+ _users.size());
		return updateFullMode;
	}

	private RenderViewData getRenderByParticipantId( String participantId)
	{
		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			if( d._participant != null && participantId.equals( d._participant.getParticipantId()))
				return d;
		}
		return null;
	}

	public void Resume()
	{
		Log.d( TAG, "Resume: on_pause=" + _on_pause);
		_on_pause = false;

		for( int i = 0; i < _renders.size(); i++)
		{
			RenderViewData d = _renders.valueAt( i);
			String user = getParticipantByViewId( d._view_id);
			if( d._video_on && user != null)
			{
				Log.d( TAG, "resume GLview " + d._view_id + " for user " + user);
				try
				{
					ConferenceCore.instance().receiveParticipantVideoOn( user);
				} catch( CoreException e)
				{
					Log.e( TAG, prefix() + "error: " + e.toString());
				}
			}
		}
	}

	public void setVideoOn( String participant, boolean on)
	{
		LogSdk.i(TAG, prefix() + "start -> " + participant);
		VideoParticipant p = _users.get( participant.hashCode());
		int view_id = p != null ? p.getViewId() : 0;
		if( view_id != 0 && view_id != -1)
		{
			RenderViewData d = _renders.get( view_id);
			if( d != null)
			{
				d.setVideoOn( on);
				Log.d( TAG,
						prefix() + "Set video to " + on + " for participant "
								+ (p == null ? participant : p.toString()));
			}
		}
		else
		{
			RenderViewData d = getFreeRender( p.getParticipantId());
			if( d != null)
			{
				p.setViewId( d._view_id);
				d.setParticipant( p);
				d.setVideoOn( on);
				Log.d( TAG,
						prefix() + "Set video to " + on + " for participant "
								+ (p == null ? participant : p.toString()));
			}
			else
			{
				Log.e( TAG,
						prefix() + "Set video to " + on + " for participant "
								+ (p == null ? participant : p.toString()) + " FAILED! viewId = " + view_id);
			}
		}
		LogSdk.i( TAG, prefix() + "stop <- " + participant);
	}

	public void turnVideoOff( String sParticipantId)
	{
		LogSdk.i( TAG, prefix() + "start -> turning video OFF for: " + sParticipantId);
		VideoParticipant p = _users.get( sParticipantId.hashCode());
		if( p == null)
		{
			Log.e( TAG, prefix() + "Participant " + sParticipantId + " not found");
			LogSdk.i( TAG, prefix() + "stop <- turning video OFF for: " + sParticipantId);
			return;
		}

		int view_id = p.getViewId();
		if( view_id == -1)
		{
			Log.w( TAG, prefix() + "No GLview found for participant " + p.toString());
			LogSdk.i( TAG, prefix() + "stop <- turning video OFF for: " + sParticipantId);
			return;
		}

		RenderViewData d = _renders.get( view_id);
		if( d != null)
		{
			if( d._view != null)
				d.disconnect();

			Log.d( TAG, prefix() + "video is OFF for " + p.toString());
		}
		else
			Log.w( TAG, prefix() + "turnVideoOff: No GVView found for Participant " + p.toString());
		LogSdk.i( TAG, prefix() + "stop <- turning video OFF for: " + sParticipantId);
	}

	public boolean turnVideoOn( String sParticipantId, boolean is_preview)
	{
		LogSdk.i( TAG, prefix() + "start -> " + sParticipantId);
		VideoParticipant p = _users.get( sParticipantId.hashCode());
		if( p == null)
		{
			LogSdk.e( TAG, prefix() + "Participant " + sParticipantId + " not found");
			LogSdk.i( TAG, prefix() + "stop <- " + sParticipantId);
			return false;
		}

		RenderViewData d = null;
		int view_id = p.getViewId();
		if( view_id != -1)
		{
			String fp = getParticipantByViewId( view_id);
			if( fp != null && !fp.equals( p.getParticipantId()))
			{
				view_id = -1;
				p.setViewId( view_id);
			}
		}
		if( view_id == -1)
		{
			LogSdk.d( TAG, prefix() + "Participant " + p.toString() + " is not connected to any GLView");

			d = getFreeRender( p.getParticipantId());
		}
		else
		{
			d = _renders.get( view_id);
		}

		if( d != null)
		{
			if( d._view != null)
			{
				if( d._view.isConnected())
				{
					LogSdk.w( TAG, prefix() + "current GLViews already connected: " + d.toString());
					LogSdk.i( TAG, prefix() + "stop <- " + sParticipantId);
					return true;
				}
				else
				{
					d._view.setPreview( d._isPreview);
					if( d._view.connect( sParticipantId))
					{
						d._video_on = true;
						p.setViewId( d._view_id);
						d.setParticipant( p);
						LogSdk.d( TAG, prefix() + "video is ON for GLView " + d.toString());

						LogSdk.w( TAG, prefix() + "current GLViews:\n" + toString( prefix()));
						LogSdk.i( TAG, prefix() + "stop <- " + sParticipantId);
						return true;
					}
					else
					{
						LogSdk.e( TAG, prefix() + "connect failed for " + p.toString());
						d._video_on = false;
						d.disconnect();
						p.setViewId( -1);
						d.setParticipant( null);
					}
				}
			}
			else
				LogSdk.w( TAG, prefix() + "GLview is NULL");
		}
		else
		{
			LogSdk.w( TAG, prefix() + "No available GLView found for Participant " + sParticipantId);
			LogSdk.w( TAG, prefix() + "current GLViews:\n" + toString());
		}
		LogSdk.i( TAG, prefix() + "stop <- " + sParticipantId);
		return false;
	}

	public void updateGLView( int view_id, SessionUIPresenter presenter, IConferenceCore core, String myId)
	{
		boolean isPreview = view_id == R.id.myVideoSurface || view_id == R.id.preview_layout_id ? true : false;
		RenderViewData d = new RenderViewData( view_id, isPreview);

		ooVooSdkGLSurfaceView glview = (ooVooSdkGLSurfaceView) presenter.findViewById( d._view_id);
		if( glview == null)
		{
			Log.e( TAG, prefix() + "NULL GL view!!! " + d._view_id);
			return;
		}

		glview.setCoreInstance( core);
		glview.setPreview( isPreview);
		d.setView( glview);

		_renders.put( view_id, d);
		Log.d( TAG, prefix() + "adding GLview " + view_id + " total = " + _renders.size());

		for( int i = 0; i < _users.size(); i++)
		{
			VideoParticipant p = _users.valueAt( i);
			if( p.getViewId() == view_id)
				turnVideoOn( p.getParticipantId(), p.getParticipantId().equals( myId));

		}

	}
}
