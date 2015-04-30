package net.omplanet.starwheel.model.api;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import net.omplanet.starwheel.model.domain.Community;
import net.omplanet.starwheel.model.network.GsonRequest;
import net.omplanet.starwheel.model.network.RequestManager;

public class ApiManager {
    public static final String REQUEST_TAG_MAIN = "MainActivityRequests";
    public static final String REQUEST_TAG_MAIN_MESSAGES = "MainActivityMessagesRequests";

    private final String TAG = getClass().getSimpleName();
	private static ApiManager mInstance;

	public static ApiManager getInstance(){
		if(mInstance == null) {
			mInstance = new ApiManager();
		}

		return mInstance;
	}

    public <T> void getMessageData(Listener<MessageData> listener, ErrorListener errorListener, String username, int count, String maxId, Object requestTag){
        Uri.Builder uriBuilder = Uri.parse(APIConstants.API_BASE + APIConstants.THING + APIConstants.DATA + APIConstants.MESSAGE + APIConstants.DATA_JSON).buildUpon()
                .appendQueryParameter(APIConstants.USERNAME, username)
                .appendQueryParameter(APIConstants.COUNT, "" + count);

        if(maxId != null) {
            uriBuilder.appendQueryParameter(APIConstants.MAX_ID, maxId);
        }

        String uri = uriBuilder.build().toString();
        GsonRequest<MessageData> request = new GsonRequest<MessageData>(Method.GET
                , uri
                , MessageData.class
                , listener
                , errorListener);

        // Set the tag on the request.
        request.setTag(requestTag);

        Log.d(TAG, "getMessageData() request:" + request.toString());
        RequestManager.getRequestQueue().add(request);
    }

    public <T> void getCommunity(Listener<Community> listener, ErrorListener errorListener, String id, Object requestTag){
        Uri.Builder uriBuilder = Uri.parse(APIConstants.API_BASE + APIConstants.THING + APIConstants.COMMUNITY + APIConstants.IDS_JSON).buildUpon()
                .appendQueryParameter(APIConstants.ID, id);

        String uri = uriBuilder.build().toString();
        GsonRequest<Community> request = new GsonRequest<Community>(Method.GET
                , uri
                , Community.class
                , listener
                , errorListener);

        // Set the tag on the request.
        request.setTag(requestTag);

        Log.d(TAG, "getCommunity() request:" + request.toString());
        RequestManager.getRequestQueue().add(request);
    }

    public <T> void getCommunityData(Listener<CommunityData> listener, ErrorListener errorListener, String id, Object requestTag){
        Uri.Builder uriBuilder = Uri.parse(APIConstants.API_BASE + APIConstants.THING + APIConstants.COMMUNITY + APIConstants.DATA_JSON).buildUpon()
                .appendQueryParameter(APIConstants.ID, id);

        String uri = uriBuilder.build().toString();
        GsonRequest<CommunityData> request = new GsonRequest<CommunityData>(Method.GET
                , uri
                , CommunityData.class
                , listener
                , errorListener);

        // Set the tag on the request.
        request.setTag(requestTag);

        Log.d(TAG, "getCommunityData() request:" + request.toString());
        RequestManager.getRequestQueue().add(request);
    }

}
