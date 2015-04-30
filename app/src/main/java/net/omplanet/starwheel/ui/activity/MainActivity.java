package net.omplanet.starwheel.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.model.api.ApiManager;
import net.omplanet.starwheel.model.domain.Person;
import net.omplanet.starwheel.model.domain.Seed;
import net.omplanet.starwheel.model.domain.Thing;
import net.omplanet.starwheel.model.hexagonalgrids.Cube;
import net.omplanet.starwheel.model.hexagonalgrids.FlowerGrid;
import net.omplanet.starwheel.model.imagecache.ImageCacheManager;
import net.omplanet.starwheel.model.network.RequestManager;
import net.omplanet.starwheel.ooVoo.ConferenceManager;
import net.omplanet.starwheel.ooVoo.Settings.UserSettings;
import net.omplanet.starwheel.ui.fragment.HexGridViewFragment;
import net.omplanet.starwheel.ui.fragment.MyMapFragment;
import net.omplanet.starwheel.ui.fragment.SlidingTabsColorsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements HexGridViewFragment.OnGridFragmentInteractionListener, MyMapFragment.OnMapFragmentInteractionListener {
    public final static String TAG = MainActivity.class.getName();
    public final static String ARG_MY_PERSON_ID = "myPersonId";

    // Whether the details Fragment is currently shown
    private boolean mMapShown = false;
    //Map objects
    List<Thing> thingsOnMap = new ArrayList();
    private HexGridViewFragment mHexGridViewFragment;
    private MyMapFragment mMyMapFragment;

    private String myPersonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            //Init the arguments
            myPersonId = savedInstanceState.getString(ARG_MY_PERSON_ID);
//            mCurrentPersonCube = new Cube(savedInstanceState.getString(ARG_SELECTED_PERSON_CUBE));
//            mCurrentSeedCube = new Cube(savedInstanceState.getString(ARG_SELECTED_SEED_CUBE));
//            mCurrentGridRadius = savedInstanceState.getInt(ARG_SELECTED_GRID_RADIUS);

            mHexGridViewFragment = (HexGridViewFragment) getSupportFragmentManager().findFragmentByTag("GridViewFragment");
            mMyMapFragment = (MyMapFragment) getSupportFragmentManager().findFragmentByTag("MyMapFragment");
        } else {
            //Init the arguments
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                myPersonId = extras.getString(MainActivity.ARG_MY_PERSON_ID);
//            mCurrentPersonCube = new Cube(getArguments().getString(ARG_SELECTED_PERSON_CUBE));
//            mCurrentSeedCube = new Cube(getArguments().getString(ARG_SELECTED_SEED_CUBE));
//            mCurrentGridRadius = getArguments().getInt(ARG_SELECTED_GRID_RADIUS);
            }

            //Init the fragments
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            mHexGridViewFragment = HexGridViewFragment.newInstance(myPersonId);
            transaction.replace(R.id.main_grid_view_fragment, mHexGridViewFragment, "GridViewFragment");

            mMyMapFragment = MyMapFragment.newInstance(null, null);
            transaction.replace(R.id.main_map_fragment, mMyMapFragment, "MyMapFragment");

            SlidingTabsColorsFragment slidingTabsColorsFragment = new SlidingTabsColorsFragment();
            transaction.replace(R.id.main_content_fragment, slidingTabsColorsFragment);

            transaction.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putString(ARG_MY_PERSON_ID, myPersonId);
//        outState.putString(ARG_SELECTED_PERSON_CUBE, mCurrentPersonCube.toString());
//        outState.putString(ARG_SELECTED_SEED_CUBE, mCurrentSeedCube.toString());
//        outState.putInt(ARG_SELECTED_GRID_RADIUS, mCurrentGridRadius);
    }

    @Override
    public void onStop() {
        super.onStop();
        RequestManager.getRequestQueue().cancelAll(ApiManager.REQUEST_TAG_MAIN);
        RequestManager.getRequestQueue().cancelAll(ApiManager.REQUEST_TAG_MAIN_MESSAGES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem detailsToggle = menu.findItem(R.id.menu_toggle_details);
        boolean isPortraitMode = (findViewById(R.id.main_viewanimator) instanceof ViewAnimator);
        detailsToggle.setVisible(isPortraitMode);
        detailsToggle.setTitle(mMapShown ? R.string.menu_hide_map : R.string.menu_show_map);
        detailsToggle.setIcon(mMapShown ? R.drawable.ic_action_group : R.drawable.ic_action_map);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_details:
                changeFragments();
                return true;
//            case R.id.menu_action_search:
//                return true;
//            case R.id.menu_action_settings:
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFragments() {
        mMapShown = !mMapShown;
        ViewAnimator output = (ViewAnimator) findViewById(R.id.main_viewanimator);
        if (mMapShown) {
            output.setDisplayedChild(1);
            mMyMapFragment.onMapObjectsChanged(thingsOnMap);
        } else {
            output.setDisplayedChild(0);
        }
        supportInvalidateOptionsMenu();
    }

    //HexGridViewFragment interface methods///////////////////////////////////

    // Reload grid fragment with new gridRadius
    //TODO change the contents of all fragments
    @Override
    public void onGridChanged(Cube personCube, Cube seedCube, int radius, FlowerGrid flowerGrid) {
        if(mHexGridViewFragment == null || flowerGrid == null) return;

        //Map objects
        thingsOnMap.clear();

        switch (radius) {
            case 0:
                thingsOnMap.add(flowerGrid.getPersonByCube(personCube));
                break;
            case 1:
                Seed currentSeed = flowerGrid.getSeedByCube(seedCube);
                thingsOnMap.add(currentSeed);
                for(String personId : currentSeed.getPersons()) {
                    thingsOnMap.add(flowerGrid.getPersons().get(personId));
                }
                break;
            default:
                for(Person person : flowerGrid.getPersons().values()) {
                    thingsOnMap.add(person);
                }
                for(Seed seed : flowerGrid.getSeeds().values()) {
                    thingsOnMap.add(seed);
                }
        }

        boolean isPortraitMode = (findViewById(R.id.main_viewanimator) instanceof ViewAnimator);
        if (!isPortraitMode || mMapShown) {mMyMapFragment.onMapObjectsChanged(thingsOnMap);};

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        HexGridViewFragment hexGridViewFragment = HexGridViewFragment.newInstance(myPersonId, mCurrentPersonCube, mCurrentSeedCube, mCurrentGridRadius);
//        transaction.replace(R.id.main_grid_view_fragment, hexGridViewFragment, "GridViewFragment");
//        transaction.commit();
    }

    @Override
    public void onActionButtonClicked(String displayName, String sessionId, boolean isCameraMuted) {
        Log.i(TAG, "Init ConferenceManager");
        ConferenceManager conferenceManager = ConferenceManager.getInstance(this);

        if( conferenceManager != null && sessionId != null && displayName != null) {
            // Read settings
            UserSettings settingsToPersist = conferenceManager.retrieveSettings();
            settingsToPersist.UserID = myPersonId;
			settingsToPersist.DisplayName = displayName;
			settingsToPersist.SessionID = sessionId;
			settingsToPersist.CameraMuted = isCameraMuted;

            // Save changes
            conferenceManager.persistSettings(settingsToPersist);

            Log.i(TAG, "Start Conference with SessionID: " + sessionId);
            Intent intent = new Intent(MainActivity.this, OoVooActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onTitleAndBgChanged(String title, String subtitle, String url) {
        ActionBar ab = getActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        ab.setTitle(title);
        ab.setSubtitle(subtitle);

        ImageLoader imageLoader = ImageCacheManager.getInstance().getImageLoader();
        NetworkImageView view = (NetworkImageView) findViewById(R.id.main_background_image);
        view.setImageUrl(url, imageLoader);
    }

    //MyMapFragment interface methods////////////////////////
    @Override
    public void onMapInteraction(Uri uri) {
        Log.i(TAG, "onFragmentInteraction() Uri: " + uri);
    }
}
