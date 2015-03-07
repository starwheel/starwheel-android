package net.omplanet.starwheel.api;


import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.omplanet.starwheel.R;

/**
 * A generic Activity that instantiates a RESTLoader to make API calls and handle the response.
 *
 */
public abstract class RESTLoaderActivity extends Activity implements LoaderManager.LoaderCallbacks<RESTLoader.RESTResponse> {
    private static final String TAG = RESTLoaderActivity.class.getName();

    protected static final String ARGS_VERB    = "RESTLoaderActivity.ARGS_VERB";
    protected static final String ARGS_URI     = "RESTLoaderActivity.ARGS_URI";
    protected static final String ARGS_PARAMS  = "RESTLoaderActivity.ARGS_PARAMS";

    protected ProgressDialog progDialog;
    protected static int loaderId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Init the REST call parameters.
//        Bundle params = generateRequestParams();
//
//        // Init the RESTLoader with the right arguments
//        Bundle args = generateArgsBundle(params);
//        getLoaderManager().initLoader(loaderId, args, this);
    }

    @Override
    public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
            RESTLoader.HTTPVerb verb = RESTLoader.HTTPVerb.values()[args.getInt(ARGS_VERB)];
            Uri    action = args.getParcelable(ARGS_URI);
            Bundle params = args.getParcelable(ARGS_PARAMS);

            return new RESTLoader(this, verb, action, params, false);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<RESTLoader.RESTResponse> loader, RESTLoader.RESTResponse data) {
        int    code = data.getCode();
        String response = data.getData();

        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !response.equals("")) {
            handleResponse(response);
        }
        else {
            Log.e(TAG, "RESTResponse code = " + code + " response = " + response);
            handleResponse(response);
        }
    }

    @Override
    public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {
        Log.d(TAG, "onLoaderReset called.");
    }

    protected void setInProgress(boolean inProgress) {
        setInProgress(inProgress, getString(R.string.please_wait));
    }

    protected void setInProgress(boolean inProgress, String message) {
        if(inProgress) {
            progDialog = new ProgressDialog(this);
            progDialog.setMessage(message);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setIndeterminate(false);
            progDialog.setCancelable(false);
            progDialog.show();
        } else {
            if (progDialog != null) progDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progDialog != null) progDialog.dismiss();
    }

    //Methods to be implemented by the child activities
    protected abstract Bundle generateArgsBundle(Bundle params);

    protected abstract Bundle generateRequestParams();

    protected abstract void handleResponse(String response);
}
