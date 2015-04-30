//
// ParticipantsManager.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.SparseArray;

import net.omplanet.starwheel.ooVoo.SessionUIPresenter;
import net.omplanet.starwheel.ooVoo.Common.ParticipantHolder.VideoParticipant;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;

public class ParticipantsManager
{

	private static class DefaultParticipantComparator implements Comparator<Participant>
	{

		@Override
		public int compare( Participant lhs, Participant rhs)
		{
			return lhs.getDisplayName().compareTo( rhs.getDisplayName());
		}

	}

	private ParticipantHolder		_pholder						= null;

	public static final int			MAX_ACTIVE_PARTICIPANTS_IN_CALL	= 4;

	private Comparator<Participant>	mParticipantComparator			= new DefaultParticipantComparator();

	public ParticipantsManager()
	{
		_pholder = new ParticipantHolder();
	}

	public void destroy()
	{
		if( _pholder != null)
			_pholder.clear();
		_pholder = null;
	}

	public String findRenderIdByViewId( int view_id)
	{
		return _pholder.findRenderIdByViewId( view_id);
	}

	public int getNoOfVideosOn()
	{
		return _pholder.getNumOfVideosOn();
	}

	public Participant getParticipant( String sParticiapntId)
	{
		Participant retParticipant = null;
		VideoParticipant participant = _pholder.getParticipant( sParticiapntId);
		if( participant != null)
		{
			retParticipant = new Participant( participant.getParticipantId(), participant.getOpaqueString(),
					_pholder.isVideoOn( participant.getParticipantId()));
		}
		return retParticipant;
	}

	public List<Participant> getParticipants()
	{
		SparseArray<VideoParticipant> participants = _pholder.getParticipants();
		List<Participant> plist = new ArrayList<Participant>();

		for( int i = 0; i < participants.size(); i++)
		{
			VideoParticipant p = participants.valueAt( i);
			plist.add( new Participant( p.getParticipantId(), p.getOpaqueString(), _pholder.isVideoOn( p
					.getParticipantId())));
		}
		Collections.sort( plist, mParticipantComparator);
		return plist;
	}

	public void onLeftSession( ConferenceCoreError eErrorCode)
	{
		_pholder.removeParticipants();
		_pholder.clear();
	}

	public void onParticipantJoinedSession( String sParticipantId, String sOpaqueString, int view_id)
	{
		_pholder.addParticipant( sParticipantId, sOpaqueString, view_id);
	}

	public boolean onParticipantLeftSession( String sParticipantId)
	{
		return _pholder.removeParticipant( sParticipantId);
	}

	public void OnParticipantVideoTurnedOff( ConferenceCoreError eErrorCode, String sParticipantId)
	{

		if( eErrorCode == ConferenceCoreError.OK)
		{
			_pholder.setVideoOn( sParticipantId, false);
		}
	}

	public void OnParticipantVideoTurnedOn( ConferenceCoreError eErrorCode, String sParticipantId, FrameSize frameSize)
	{

		if( eErrorCode == ConferenceCoreError.OK)
		{
			_pholder.setVideoOn( sParticipantId, true);
		}
	}

	public int prepareParticipantAsActiveRender( String sParticipantId)
	{
		return _pholder.prepareParticipantAsActiveRender( sParticipantId);
	}

	public boolean isFullMode()
	{
		return _pholder.isFullMode();
	}

	public void moveToFullMode( int viewId)
	{
		_pholder.moveToFullMode( viewId);
	}

	public void moveToMultiMode( int viewId)
	{
		_pholder.moveToMultiMode( viewId);
	}

	public void setVideoOn( String id, boolean b)
	{
		_pholder.setVideoOn( id, b);
	}

	public void turnVideoOff( String id)
	{
		_pholder.turnVideoOff( id);
	}

	public boolean isVideoOn( int viewId)
	{
		return _pholder.isVideoOn( viewId);
	}

	public boolean isRenderActive( int viewId)
	{
		return _pholder.isRenderActive( viewId);
	}

	public void addParticipant( String localParticipantIdDefault, String string, int i)
	{
		_pholder.addParticipant( localParticipantIdDefault, string, i);
	}

	public int getViewIdForFullMode()
	{
		return _pholder.getViewIdForFullMode();
	}

	public void updateGLView( int id, SessionUIPresenter activity, IConferenceCore mConferenceCore, String myId)
	{
		_pholder.updateGLView( id, activity, mConferenceCore, myId);
	}

	public int getViewIdByParticipant( String id)
	{
		return _pholder.getViewIdByParticipant( id);
	}

	public String getParticipantByViewId( int id)
	{
		return _pholder.getParticipantByViewId( id);
	}

	public boolean isFullMode( String sParticipantId)
	{
		return _pholder.isFullMode( sParticipantId);
	}

	public void Pause()
	{
		_pholder.Pause();
	}

	public boolean turnVideoOn( String sParticipantId, boolean is_preview)
	{
		return _pholder.turnVideoOn( sParticipantId, is_preview);
	}

	public void Resume()
	{
		_pholder.Resume();
	}

	public boolean isVideoOn( String id)
	{
		return _pholder.isVideoOn( id);
	}

	public ParticipantHolder getHolder()
	{
		return _pholder;
	}

}
