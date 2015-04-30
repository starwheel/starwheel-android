package net.omplanet.starwheel;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;

import net.omplanet.starwheel.model.imagecache.ImageCacheManager;
import net.omplanet.starwheel.model.network.RequestManager;
import net.omplanet.starwheel.ooVoo.ConferenceManager;

public class MainApplication extends Application {
	private static Application mInstance;

	private static int DISK_IMAGECACHE_SIZE = 1024*1024*10;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided

	private ConferenceManager	mConferenceManager	= null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		initVolley();
		initOoVoo();
	}

	/**
	 * Intialize the request manager and the image cache 
	 */
	private void initVolley() {
		RequestManager.init(this);
		createImageCache();
	}
	
	/**
     * Create the image cache for Volley. Uses Memory Cache by default. Change to Disk for a Disk based LRU implementation.
	 */
	private void createImageCache(){
		ImageCacheManager.getInstance().init(this,
				this.getPackageCodePath()
				, DISK_IMAGECACHE_SIZE
				, DISK_IMAGECACHE_COMPRESS_FORMAT
				, DISK_IMAGECACHE_QUALITY
				, ImageCacheManager.CacheType.MEMORY);
	}

	/**
	 * Intialize the ooVoo
	 */
	private void initOoVoo() {

		try {
			if( mConferenceManager  == null) {
                mConferenceManager = ConferenceManager.getInstance(getApplicationContext());
                mConferenceManager.initConferenceCore();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Resources getApplicationResources() {
		return mInstance.getResources();
	}
}