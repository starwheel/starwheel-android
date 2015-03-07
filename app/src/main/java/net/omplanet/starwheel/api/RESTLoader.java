package net.omplanet.starwheel.api;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RESTLoader extends AsyncTaskLoader<RESTLoader.RESTResponse> {
    private static final String TAG = RESTLoader.class.getName();

    // We use this delta to determine if our cached data is
    // old or not. The value we have here is 1 hour;
    private static final long STALE_DELTA = 60 * 60 * 1000L;
    private static final int HTTP_CONNECTION_TIMEOUT = 10 * 1000;
    private static final int SOCKET_CONNECTION_TIMEOUT = 20 * 1000;

    public enum HTTPVerb {
        GET,
        POST,
        PUT,
        DELETE
    }

    public static class RESTResponse {
        private String mData;
        private int    mCode;

        public static int REST_LOADER_UNKNOWN_ERROR = 600;

        public RESTResponse() {
            mData = null;
            mCode = REST_LOADER_UNKNOWN_ERROR;
        }

        public RESTResponse(String data, int code) {
            mData = data;
            mCode = code;
        }

        public String getData() {
            return mData;
        }

        public int getCode() {
            return mCode;
        }
    }

    private HTTPVerb     mVerb;
    private Uri          mAction;
    private Bundle       mParams;
    private boolean      mPersistResult;
    private RESTResponse mRestResponse;

    private long mLastLoad;

    public RESTLoader(Context context) {
        super(context);
    }

    public RESTLoader(Context context, HTTPVerb verb, Uri action) {
        super(context);

        mVerb   = verb;
        mAction = action;
    }

    public RESTLoader(Context context, HTTPVerb verb, Uri action, Bundle params, boolean persistResult) {
        super(context);

        mVerb   = verb;
        mAction = action;
        mParams = params;
        mPersistResult = persistResult;
    }

    @Override
    public RESTResponse loadInBackground() {
        try {
            // At the very least we always need an action.
            if (mAction == null) {
                return new RESTResponse("RESTLoader Action is not defined. REST call canceled.",RESTResponse.REST_LOADER_UNKNOWN_ERROR);
                // TODO implementation will always need to check the RESTResponse
                // and handle error cases like this.
            }

            // Try to get cashed results
            if (mPersistResult) {
                /*try {
                    FileInputStream is = DownloadCacheQueue.getFile(mAction, getContext());
                    byte [] data = Utilities.convertInputStreamToByteArray(is);
                    int statusCode = 200; //OK

                    // Here we create our response to send it back to the LoaderCallbacks<RESTResponse> implementation.
                    RESTResponse restResponse = new RESTResponse(data, statusCode);

                    // If cache worked out well, we don't go to network connection.
                    Log.d(TAG, "Cached result:\\n" + data);
                    return restResponse;
                } catch (Exception e) {
                    Log.w(TAG, "Using network connection due to exception when using cache: " + e.getLocalizedMessage());
                }*/
            }

            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request = null;

            // Let's build our request based on the HTTP verb we were given.
            switch (mVerb) {
                case GET: {
                    request = new HttpGet();
                    attachUriWithQuery(request, mAction, mParams);
                    //request.addHeader("client_id", CLIENT_ID);

                }
                break;

                case DELETE: {
                    request = new HttpDelete();
                    attachUriWithQuery(request, mAction, mParams);
                    //request.addHeader("client_id", CLIENT_ID);

                }
                break;

                case POST: {
                    request = new HttpPost();
                    request.setURI(new URI(mAction.toString()));
                    //request.addHeader("client_id", CLIENT_ID);

                    // Attach the JSON entity if necessary.
                    HttpPost postRequest = (HttpPost) request;
                    attachStringEntity(postRequest, mParams);
                }
                break;

                case PUT: {
                    request = new HttpPut();
                    request.setURI(new URI(mAction.toString()));
                    //request.addHeader("client_id", CLIENT_ID);

                    // Attach the JSON entity if necessary.
                    HttpPut putRequest = (HttpPut) request;
                    attachStringEntity(putRequest, mParams);
                }
                break;
            }

            if (request != null) {
                HttpClient client = new DefaultHttpClient();
                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(params, HTTP_CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, SOCKET_CONNECTION_TIMEOUT);

                // Let's send some useful debug information so we can monitor things
                // in LogCat.
                Log.d(TAG, "Executing request: "+ verbToString(mVerb) +": "+ mAction.toString());

                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this Loader's thread.
                HttpResponse response = client.execute(request); //TODO investigate IOException crash when no internet

                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;

                // Here we create our response and send it back to the LoaderCallbacks<RESTResponse> implementation.
                RESTResponse restResponse = new RESTResponse(responseEntity != null ? EntityUtils.toString(responseEntity) : null, statusCode);
                return restResponse;
            }

            // Request was null if we get here, so let's just send our error RESTResponse like usual.
            return new RESTResponse("RESTLoader Request is null", RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ verbToString(mVerb) +": "+ mAction.toString(), e);
            return new RESTResponse(e.toString(), RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            return new RESTResponse(e.toString(), RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTResponse(e.toString(), RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        }
        catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            return new RESTResponse(e.toString(), RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        } catch (Exception e) {
            Log.e(TAG, "RESTLoader failed to execute the network request.", e);
            return new RESTResponse(e.toString(), RESTResponse.REST_LOADER_UNKNOWN_ERROR);
        }
    }

    @Override
    public void deliverResult(RESTResponse data) {
        // Here we cache our response.
        mRestResponse = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (mRestResponse != null) {
            // We have a cached result, so we can just
            // return right away.
            super.deliverResult(mRestResponse);
        }

        // If our response is null or we have hung onto it for a long time,
        // then we perform a force load.
        if (mRestResponse == null || System.currentTimeMillis() - mLastLoad >= STALE_DELTA) forceLoad();
        mLastLoad = System.currentTimeMillis();
    }

    @Override
    protected void onStopLoading() {
        // This prevents the AsyncTask backing this
        // api from completing if it is currently running.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Stop the Loader if it is currently running.
        onStopLoading();

        // Get rid of our cache if it exists.
        mRestResponse = null;

        // Reset our stale timer.
        mLastLoad = 0;
    }

    private static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            }
            else {
                Uri.Builder uriBuilder = uri.buildUpon();

                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : paramsToList(params)) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }

                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString());
        }
    }

    private static void attachStringEntity(HttpEntityEnclosingRequestBase request, Bundle params) {
        try {
            if (params != null) {
                StringEntity entity = new StringEntity(params.getString("stringEntity"));
                String contentType = params.getString("contentType");

                entity.setContentEncoding("UTF-8");
                entity.setContentType(contentType);

                request.setEntity(entity);

                Log.d(TAG, "String entity: "+ entity);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Bundle string entity parameters were incorrect: "+ params);
        }
    }

    private static String verbToString(HTTPVerb verb) {
        switch (verb) {
            case GET:
                return "GET";

            case POST:
                return "POST";

            case PUT:
                return "PUT";

            case DELETE:
                return "DELETE";
        }

        return "";
    }

    private static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());

        for (String key : params.keySet()) {
            Object value = params.get(key);

            // We can only put Strings in a form entity, so we call the toString()
            // method to enforce. We also probably don't need to check for null here
            // but we do anyway because Bundle.get() can return null.
            if (value != null) formList.add(new BasicNameValuePair(key, value.toString()));
        }

        return formList;
    }
}
