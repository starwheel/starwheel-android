package net.omplanet.starwheel.ui.navigation.slidingtabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewAnimator;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.ui.MainOoVooActivity;
import net.omplanet.starwheel.ui.navigation.common.activities.SampleActivityBase;

public class MainActivity extends SampleActivityBase {
    public final String TAG = getClass().getName();

    // Whether the details Fragment is currently shown
    private boolean mDetailsShown = false;
    // The current radius of the grid view
    private int mCurrentRadius = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            SlidingTabsColorsFragment slidingTabsColorsFragment = new SlidingTabsColorsFragment();
            transaction.replace(R.id.sample_content_fragment, slidingTabsColorsFragment);

            GridViewFragment gridViewFragment = new GridViewFragment();
            Bundle args = new Bundle();
            args.putInt(GridViewFragment.ARG_RADIUS, mCurrentRadius);
            gridViewFragment.setArguments(args);
            transaction.replace(R.id.grid_view_fragment, gridViewFragment, "GridViewFragment");

            transaction.commit();
        }
    }

    // Reload grid fragment with new radius
    //TODO change the contents of all fragments
    public void reloadGrid(int radius) {
        mCurrentRadius = radius;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        GridViewFragment gridViewFragment = new GridViewFragment();
        Bundle args = new Bundle();
        args.putInt(GridViewFragment.ARG_RADIUS, mCurrentRadius);
        gridViewFragment.setArguments(args);
        transaction.replace(R.id.grid_view_fragment, gridViewFragment, "GridViewFragment");

        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem detailsToggle = menu.findItem(R.id.menu_toggle_details);
        detailsToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        detailsToggle.setTitle(mDetailsShown ? R.string.sample_hide_details : R.string.sample_show_details);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_details:
                mDetailsShown = !mDetailsShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mDetailsShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, MainOoVooActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
