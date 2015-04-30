package net.omplanet.starwheel.ooVoo.VideoCall;

public class CameraWrapper 
{
	public enum CameraState 
	{
		BACK_CAMERA, FRONT_CAMERA, MUTE_CAMERA;
		
		public static CameraState fromInt( int e)
		{
			switch( e)
			{
			case 0:
				return BACK_CAMERA;
			case 1:
				return FRONT_CAMERA;
			case 2:
				return MUTE_CAMERA;
			default:
				return FRONT_CAMERA;
			}
		}
		
		public static int toInt( CameraState e)
		{
			switch( e)
			{
			case BACK_CAMERA:
				return 0;
			case FRONT_CAMERA:
				return 1;
			case MUTE_CAMERA:
				return 2;
			default:
				return 1;
			}
		}
	}
}
