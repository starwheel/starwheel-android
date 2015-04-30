package net.omplanet.starwheel.ooVoo.VideoCall;

import com.oovoo.core.phone.AudioRouteManager.AudioRoute;

public class AudioRouteWrapper 
{
	public static AudioRoute fromInt( int e)
	{	
		switch( e)
		{
		case 0:
			return AudioRoute.Earpiece;
		case 1:
			return AudioRoute.Speaker;
		case 2:
			return AudioRoute.Headset;
		case 3:
			return AudioRoute.Bluetooth;
		default:	
			return AudioRoute.Speaker;
		}
	}
	
	public static int toInt( AudioRoute e)
	{
		switch( e)
		{
		case Earpiece:
			return 0;
		case Speaker:
			return 1;
		case Headset:
			return 2;
		case Bluetooth:
			return 3;
		default:
			return 1;
		}
	}
}
