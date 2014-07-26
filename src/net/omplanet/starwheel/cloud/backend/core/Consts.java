package net.omplanet.starwheel.cloud.backend.core;

/**
 * A class to hold hard coded constants. Developers may modify the values based
 * on their usage.
 */
public interface Consts {

    /**
     * Set Project ID of your Google APIs Console Project.
     */
    public static final String PROJECT_ID = "calm-path-700";//"star-ray-700";

    /**
     * Set Project Number of your Google APIs Console Project.
     */
    public static final String PROJECT_NUMBER = "194301407297";//"27732133685";

    /**
     * Set your Web Client ID for authentication at backend.
     */
    //public static final String WEB_CLIENT_ID = "27732133685-n9bo3cq0m9nmqfv5hfmsr8n1um2rniko.apps.googleusercontent.com";
    public static final String WEB_CLIENT_ID = "194301407297-ucdrs6uv7me5214vo8jc6f5aqdhnnfn4.apps.googleusercontent.com";
    
    /**
     * Set default user authentication enabled or disabled.
     */
    public static final boolean IS_AUTH_ENABLED = true;

    /**
     * Auth audience for authentication on backend.
     */
    public static final String AUTH_AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

    /**
     * Endpoint root URL
     */
    public static final String ENDPOINT_ROOT_URL = "https://" + PROJECT_ID
            + ".appspot.com/_ah/api/";

    /**
     * A flag to switch if the app should be run with local dev server or
     * production (cloud).
     */
    public static final boolean LOCAL_ANDROID_RUN = false;

    /**
     * SharedPreferences keys for starwheel-android.
     */
    public static final String PREF_KEY_STARWHEEL = "PREF_KEY_STARWHEEL";
    public static final String PREF_KEY_ACCOUNT_NAME = "PREF_KEY_ACCOUNT_NAME";

    /**
     * Tag name for logging.
     */
    public static final String TAG = "starwheel-android";
}
