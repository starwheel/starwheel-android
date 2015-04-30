//
// ResolutionWrapper.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package net.omplanet.starwheel.ooVoo.VideoCall;

import com.oovoo.core.IConferenceCore.CameraResolutionLevel;

public class ResolutionWrapper 
{
	public static CameraResolutionLevel fromInt( int e)
	{
		switch( e)
		{
		case 0:
			return CameraResolutionLevel.ResolutionLow;
		case 1:
			return CameraResolutionLevel.ResolutionMedium;
		case 2:
			return CameraResolutionLevel.ResolutionHigh;
		case 3:
			return CameraResolutionLevel.ResolutionHD;
		default:
			return CameraResolutionLevel.ResolutionMedium;
		}
	}
	
	public static int toInt( CameraResolutionLevel e)
	{
		switch( e)
		{
		case ResolutionLow:
			return 0;
		case ResolutionMedium:
			return 1;
		case ResolutionHigh:
			return 2;
		case ResolutionHD:
			return 3;
		default:
			return 1;
		}
	}
}
