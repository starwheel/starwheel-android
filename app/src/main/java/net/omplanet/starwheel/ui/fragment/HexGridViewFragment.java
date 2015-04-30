package net.omplanet.starwheel.ui.fragment;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.model.api.ApiManager;
import net.omplanet.starwheel.model.api.CommunityData;
import net.omplanet.starwheel.model.domain.Community;
import net.omplanet.starwheel.model.domain.Flower;
import net.omplanet.starwheel.model.domain.Person;
import net.omplanet.starwheel.model.domain.Seed;
import net.omplanet.starwheel.model.hexagonalgrids.Cube;
import net.omplanet.starwheel.model.hexagonalgrids.Grid;
import net.omplanet.starwheel.model.hexagonalgrids.FlowerGrid;
import net.omplanet.starwheel.model.hexagonalgrids.StorageMap;
import net.omplanet.starwheel.model.imagecache.ImageCacheManager;
import net.omplanet.starwheel.ui.activity.MainActivity;
import net.omplanet.starwheel.ui.view.CircleBorderedNetworkImageView;
import net.omplanet.starwheel.ui.view.CircleTextView;
import net.omplanet.starwheel.ui.view.LetterImageView;
import net.omplanet.starwheel.ui.view.SeedNode;
import net.omplanet.starwheel.ui.view.PersonNode;
import net.omplanet.starwheel.ui.view.SeedView;
import net.omplanet.starwheel.util.ColorUtil;

import java.util.Map;

/**
 * A fragment which contains the main hexagonal grid.
 */
public class HexGridViewFragment extends Fragment {
    public final String TAG = HexGridViewFragment.class.getName();
    public final static String ARG_MY_PERSON_ID = "myPersonId";
    public final static String ARG_SELECTED_PERSON_CUBE = "personCube";
    public final static String ARG_SELECTED_SEED_CUBE = "seedCube";
    public final static String ARG_SELECTED_GRID_RADIUS = "gridRadius";

    //Loaded community data
    private Community community;
    private FlowerGrid mFlowerGrid;

    // The selected objects and the current radius of the grid view
    private Cube mCurrentPersonCube;
    private Cube mCurrentSeedCube;
    private int mCurrentGridRadius;

    //Grid dimension properties
    private int mCurrentHexRadius;
    private int containerViewWidth;
    private int containerViewHeight;

    private String myPersonId;
    private final String mCurrentCommunityId = "onecommunity";
    private final boolean isOrientationFlatTop = true;

    private float alphaWeak;
    private float alphaMiddle;
    private float alphaStrong;

    private MainActivity mActivity;

    /**
     * @return a new instance of {@link HexGridViewFragment}, adding the parameters into a bundle and
     * setting them as arguments.
     */
    public static HexGridViewFragment newInstance(String myPersonId) {
        return newInstance(myPersonId, new Cube(0,0,0), new Cube(0,0,0), 4);
    }

   /**
     * @return a new instance of {@link HexGridViewFragment}, adding the parameters into a bundle and
     * setting them as arguments.
     */
    public static HexGridViewFragment newInstance(String myPersonId, Cube personCube, Cube seedCube, int gridRadius) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MY_PERSON_ID, myPersonId);
        bundle.putString(ARG_SELECTED_PERSON_CUBE, personCube.toString());
        bundle.putString(ARG_SELECTED_SEED_CUBE, seedCube.toString());
        bundle.putInt(ARG_SELECTED_GRID_RADIUS, gridRadius);

        HexGridViewFragment fragment = new HexGridViewFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous grid parameters set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            myPersonId = savedInstanceState.getString(ARG_MY_PERSON_ID);
            mCurrentPersonCube = new Cube(savedInstanceState.getString(ARG_SELECTED_PERSON_CUBE));
            mCurrentSeedCube = new Cube(savedInstanceState.getString(ARG_SELECTED_SEED_CUBE));
            mCurrentGridRadius = savedInstanceState.getInt(ARG_SELECTED_GRID_RADIUS);
        } else if (getArguments() != null) {
            // Set grid parameters based on argument passed in
            myPersonId = getArguments().getString(ARG_MY_PERSON_ID);
            mCurrentPersonCube = new Cube(getArguments().getString(ARG_SELECTED_PERSON_CUBE));
            mCurrentSeedCube = new Cube(getArguments().getString(ARG_SELECTED_SEED_CUBE));
            mCurrentGridRadius = getArguments().getInt(ARG_SELECTED_GRID_RADIUS);
        }

        final View view = inflater.inflate(R.layout.fragment_grid_view, container, false);

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.main_alpha_weak, outValue, true);
        alphaWeak = outValue.getFloat();
        getResources().getValue(R.dimen.main_alpha_middle, outValue, true);
        alphaMiddle = outValue.getFloat();
        getResources().getValue(R.dimen.main_alpha_strong, outValue, true);
        alphaStrong = outValue.getFloat();

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this); // API version 16 and higher.
                containerViewWidth = container.getMeasuredWidth();
                containerViewHeight = container.getMeasuredHeight();
                //Log.d("Grid Fragment:------", "window width: " + containerViewWidth + "   window height: " + containerViewHeight);

                //Load the network objects and after init the grid elements.
                if (community == null) {
                    loadCommunity(mCurrentCommunityId);
                } else {
                    initFragmentView();
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putString(ARG_MY_PERSON_ID, myPersonId);
        outState.putString(ARG_SELECTED_PERSON_CUBE, mCurrentPersonCube.toString());
        outState.putString(ARG_SELECTED_SEED_CUBE, mCurrentSeedCube.toString());
        outState.putInt(ARG_SELECTED_GRID_RADIUS, mCurrentGridRadius);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    //Init the selected Flower of Life structure with 7 Seeds
    public void initFragmentView() {
        try {
            if(mFlowerGrid.getFlower() != null) {
                //Init the container view for all of the grid elements
                final RelativeLayout gridView = (RelativeLayout) getView().findViewById(R.id.gridLayout);
                // Set grid view dimensions
                mCurrentHexRadius = setGridDimensions(gridView, containerViewWidth, containerViewHeight, mCurrentGridRadius, Grid.Shape.FLOWER_OF_LIFE);
                mFlowerGrid.setHexRadius(mCurrentHexRadius);
                //Init a grid with background elements (flower of life, seed, single person, etc.)
                initGridViewBackgroundElements(gridView, mFlowerGrid);
                //Init a grid with with the top person or seed elements
                initGridViewElements(gridView, mFlowerGrid);
                // Init grid action buttons
                setGridButtons(mFlowerGrid, gridView, getView());
                //Set breadcrumbs for the title and subtitle
                setBreadcrumbs(mFlowerGrid);
                //Inform the MainActivity
                ((OnGridFragmentInteractionListener) mActivity).onGridChanged(mCurrentPersonCube, mCurrentSeedCube, mCurrentGridRadius, mFlowerGrid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, R.string.grid_error, Toast.LENGTH_LONG).show();
            showRefreshButton();
        }
    }

    private void setBreadcrumbs(FlowerGrid flowerGrid) {
        String title = community.getName();
        String subTitle = "";
        String url = null;

        if(mCurrentGridRadius == 0) {
            Seed seed = flowerGrid.getSeedByCube(mCurrentSeedCube);
            Person person = flowerGrid.getPersonByCube(mCurrentPersonCube);
            if(seed != null && person != null) {
                subTitle = flowerGrid.getFlower().getName() + " \u27a4 " + seed.getName() + " \u27a4 " + person.getName();
                url = person.getCoverImage() == null ? person.getImage() : person.getCoverImage();
            }
        } else if(mCurrentGridRadius == 1) {
            Seed seed = flowerGrid.getSeedByCube(mCurrentSeedCube);
            if(seed != null) {
                subTitle = flowerGrid.getFlower().getName() + " \u27a4 " + seed.getName();
                url = seed.getCoverImage() == null ? seed.getImage() : seed.getCoverImage();
            }
        } else {
            subTitle = flowerGrid.getFlower().getName();
            url = flowerGrid.getFlower().getCoverImage() == null ? flowerGrid.getFlower().getImage() : flowerGrid.getFlower().getCoverImage();
        }

        ((OnGridFragmentInteractionListener) mActivity).onTitleAndBgChanged(title, subTitle, url);
    }

    private void initGridViewBackgroundElements(final RelativeLayout gridView, final FlowerGrid flowerGrid) {
        //Init a grid with background flower of life pattern (done separately to avoid
        // overdrawing of user images with flower circles)
        if(mCurrentGridRadius == 0) {
            //Add background grid node view
            final Person person = flowerGrid.getPersonByCube(mCurrentPersonCube);
            int poistionInSeed = FlowerGrid.getPersonsPositionInSeed(mCurrentPersonCube, mCurrentSeedCube);
            int bgColor = getResources().getColor(ColorUtil.getColor(poistionInSeed));
            final View nodeView = new CircleTextView(0,
                    false,
                    (person!=null?person.getName():null),
                    bgColor,
                    0,
                    getResources().getColor(R.color.main_background_border_color),
                    alphaMiddle,
                    mActivity);
            //Set the view sides as square width=height, for the background oval shape to be a circle
            int viewScale = 2;
            int sideWidth = (int) (viewScale * Math.sqrt(3) * flowerGrid.hexRadius);
            addNodeViewToGridView(sideWidth, sideWidth, nodeView, gridView, new Cube(0,0,0), flowerGrid);
        } else if(mCurrentGridRadius == 1) {
            //Generate flower grid nodes
            Cube[] flowerGridNodes = StorageMap.generateNodes(flowerGrid, true);

            //Add background grid node views
            final int viewScale = 2;
            Cube centralPersonCube = FlowerGrid.getCentralPersonCube(mCurrentSeedCube);//the central cube in the selected seed
            for(int i=0; i<flowerGridNodes.length; i++) {
                Cube cube = flowerGridNodes[i];

                final Person person = flowerGrid.getPersonByCube(cube, centralPersonCube);
                final View nodeView = new CircleTextView(i,
                        i == 0,
                        (person!=null?person.getName():null),
                        getResources().getColor(ColorUtil.getColor(i)),
                        1,
                        getResources().getColor(R.color.main_background_border_color),
                        alphaMiddle,
                        mActivity);

                //Set the view sides as square width=height, for the background oval shape to be a circle
                int sideWidth = (int) (viewScale * Math.sqrt(3) * flowerGrid.hexRadius);
                addNodeViewToGridView(sideWidth, sideWidth, nodeView, gridView, cube, flowerGrid);
            }
        } else if(mCurrentGridRadius > 1 || mCurrentGridRadius <= 4) {
            //Generate seed grid nodes////////////////////////////////
            int gridNodeScale = mCurrentGridRadius == 2 ? 2 : (mCurrentGridRadius == 3 ? 2 : 3);
            Grid seedGrid = new Grid(1,  mCurrentHexRadius * gridNodeScale, Grid.Shape.FLOWER_OF_LIFE, isOrientationFlatTop);
            Cube[] seedGridNodes = StorageMap.generateNodes(seedGrid, true);
            View.OnTouchListener gridNodeTouchListener = generateGridNodeOnTouchListener(gridView, seedGrid);
            //Add background grid views for the seed groups
            int seedViewScale = 1;
            for(int i = 0; i<seedGridNodes.length; i++) {
                Cube cube = seedGridNodes[i];
                //Seed seed = getSeedByCube(cube);
                final SeedView nodeView = new SeedView(mActivity);
                nodeView.setBackgroundResource(ColorUtil.getColorCircle(i));
                nodeView.setAlpha(alphaMiddle);
                nodeView.setCube(cube);
                nodeView.setOnTouchListener(gridNodeTouchListener);
                //Set the view sides as square width=height, for the background oval shape to be a circle
                int sideWidth = (int) (seedViewScale * Math.sqrt(3) * seedGrid.hexRadius);
                addNodeViewToGridView(sideWidth, sideWidth, nodeView, gridView, cube, seedGrid);
            }

            //Generate flower grid nodes////////////////////////////////////////
            Cube[] flowerGridNodes = StorageMap.generateNodes(flowerGrid, true);
            //Add background grid node views
            final int viewScale = 2;
            for(int i = 0; i<flowerGridNodes.length; i++) {
                Cube cube = flowerGridNodes[i];
                //final Person person = mFlowerGrid.getPersonByCube();
                final View nodeView = new CircleTextView(i,
                        i == 0,
                        null,
                        0,
                        1,
                        getResources().getColor(R.color.main_background_border_color),
                        alphaMiddle,
                        mActivity);
                //Set the view sides as square width=height, for the background oval shape to be a circle
                int sideWidth = (int) (viewScale * Math.sqrt(3) * flowerGrid.hexRadius);
                addNodeViewToGridView(sideWidth, sideWidth, nodeView, gridView, cube, flowerGrid);
            }
        } else {
            //TODO
        }
    }

    private void initGridViewElements(final RelativeLayout gridView, final FlowerGrid flowerGrid) {
        ImageLoader imageLoader = ImageCacheManager.getInstance().getImageLoader();
        View.OnTouchListener gridNodeTouchListener = null;
        if(mCurrentGridRadius == 1) {
            gridNodeTouchListener = generateGridNodeOnTouchListener(gridView, flowerGrid);
        }

        for(int i=0; i<flowerGrid.getGridNodes().length; i++) {
            Cube cube = flowerGrid.getGridNodes()[i];

            Person person = null;
            if(mCurrentGridRadius == 0) {
                person = flowerGrid.getPersonByCube(mCurrentPersonCube);
            } else if(mCurrentGridRadius >= 1) {
                Cube centralPersonCube = FlowerGrid.getCentralPersonCube(mCurrentSeedCube);//the central cube in the selected seed
                person = flowerGrid.getPersonByCube(cube, centralPersonCube);
            }

            if(person != null) {
                //Set image resource to the node
                View nodeView = null;
                String url = person.getImage();
                String name = person.getName();
                if(mCurrentGridRadius == 0) {i = FlowerGrid.getPersonsPositionInSeed(mCurrentPersonCube, mCurrentSeedCube);}
                int bgColor = getResources().getColor(ColorUtil.getColor(i));
                //person.setColor(String.format("#%06X", (0xFFFFFF & bgColor)));

                if(url != null && !url.isEmpty()) {
                    CircleBorderedNetworkImageView nodeImageView = new CircleBorderedNetworkImageView(mActivity);
                    nodeImageView.setCube(cube);
                    nodeImageView.setErrorImageResId(R.drawable.error_profile_image);
                    nodeImageView.setImageUrl(url, imageLoader);
                    nodeImageView.setBorderColor(bgColor);
                    nodeView = nodeImageView;
                } else {
                    nodeView = new LetterImageView(ColorUtil.getContrastColor(bgColor), bgColor, 1, mActivity);
                    ((LetterImageView) nodeView).setCube(cube);
                    ((LetterImageView) nodeView).setOval(true);
                    if(name != null) ((LetterImageView) nodeView).setLetter(name.charAt(0));
                }

                //Set click action
                if(mCurrentGridRadius == 1) { nodeView.setOnTouchListener(gridNodeTouchListener);}

                //Add the node to the grid
                double viewScale = (mCurrentGridRadius == 0) ? 1.5 : 0.6;
                addNodeViewToGridView((int) (flowerGrid.hexWidth * viewScale), (int) (flowerGrid.hexHeight * viewScale), nodeView, gridView, cube, flowerGrid);
            }
        }
    }

    private int setGridDimensions(View gridView, int containerViewWidth, int containerViewHeight, int gridRadius, Grid.Shape shape) {
        // Gets the layout params that will allow to resize the layout
        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        // Adjust the padding for buttons
        int margin = (int) (getResources().getDimension(R.dimen.action_button_middle)
                            + getResources().getDimension(R.dimen.action_button_margin));
        if(true) {
            containerViewWidth -= 2 * margin;
        } else {
            containerViewHeight -= 2 * margin;
        }

        //If the height is smaller then the width, use smaller width calculated according to the ratio of the shape.
        switch (shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                //int maxWidthByRatioToHeight = (int) (2 * containerViewHeight / Math.sqrt(3));
                int maxWidthByRatioToHeight = (int) (containerViewHeight * Grid.getGridWidthDividedToHeight(gridRadius, shape, isOrientationFlatTop));
                if(containerViewWidth > maxWidthByRatioToHeight) containerViewWidth = maxWidthByRatioToHeight;
                break;
            case RECTANGLE:
                if(containerViewWidth > containerViewHeight) containerViewWidth = containerViewHeight;
                break;
        }

        // Calculate the hexRadius: the radius of single hex node.
        int hexRadius = Grid.getHexRadius(gridRadius, containerViewWidth, shape, isOrientationFlatTop);

        // Changes the height and width of the grid to the specified *pixels*
        params.width = Grid.getGridWidth(gridRadius, hexRadius, shape, isOrientationFlatTop);
        params.height = Grid.getGridHeight(gridRadius, hexRadius, shape, isOrientationFlatTop);

        return hexRadius;
    }

    private void setGridButtons(final FlowerGrid flowerGrid, final ViewGroup gridView, final View mainView) {
        //int size = (containerViewWidth > containerViewHeight) ? containerViewHeight/6 : containerViewWidth/6;
//        ImageButton add = (ImageButton) mainView.findViewById(R.id.gridActionButtonAdd);
        ImageButton callButton = (ImageButton) mainView.findViewById(R.id.gridActionButtonCall);
        ImageButton videoButton = (ImageButton) mainView.findViewById(R.id.gridActionButtonVideo);
//        ImageButton editButton = (ImageButton) mainView.findViewById(R.id.gridActionButtonEdit);
        ImageButton infoButton = (ImageButton) mainView.findViewById(R.id.gridActionButtonInfo);

        final View infoView = mainView.findViewById(R.id.view_grid_info);
        final TextView infoText = (TextView) infoView.findViewById(R.id.grid_info_text);
        infoText.setText(community.getDescription());

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(infoView.getVisibility() == View.GONE) {
                    gridView.setVisibility(View.GONE);
                    infoView.setVisibility(View.VISIBLE);
                } else {
                    gridView.setVisibility(View.VISIBLE);
                    infoView.setVisibility(View.GONE);
                }
            }
        });
        switch (mCurrentGridRadius) {
            case 0:
//                add.setVisibility(View.GONE);

                Person person = flowerGrid.getPersonByCube(new Cube(0, 0, 0), mCurrentPersonCube);
                if(person == null) break;
                String sessionId = myPersonId.compareTo(person.getId()) > 0 ? myPersonId+"-"+person.getId() : person.getId()+"-"+myPersonId;
                if (myPersonId.equals(person.getId())) {
                    callButton.setVisibility(View.GONE);
                    videoButton.setVisibility(View.GONE);
//                    editButton.setVisibility(View.VISIBLE);
//                    infoButton.setVisibility(View.GONE);
                } else {
                    callButton.setVisibility(View.VISIBLE);
                    callButton.setOnClickListener(generateActionButtonOnClickListener(flowerGrid.getPersons().get(myPersonId).getName(), sessionId, true));
                    videoButton.setVisibility(View.VISIBLE);
                    videoButton.setOnClickListener(generateActionButtonOnClickListener(flowerGrid.getPersons().get(myPersonId).getName(), sessionId, false));
//                    editButton.setVisibility(View.GONE);
//                    infoButton.setVisibility(View.VISIBLE);
                }
                break;
            case 1:
//                add.setVisibility(View.VISIBLE);
//                add.setImageResource(R.drawable.ic_action_add_person);

                Seed seed = flowerGrid.getSeedByCube(mCurrentSeedCube);
                if(seed == null) break;
                callButton.setVisibility(View.VISIBLE);
                callButton.setOnClickListener(generateActionButtonOnClickListener(flowerGrid.getPersons().get(myPersonId).getName(), seed.getId(), true));
                videoButton.setVisibility(View.VISIBLE);
                videoButton.setOnClickListener(generateActionButtonOnClickListener(flowerGrid.getPersons().get(myPersonId).getName(), seed.getId(), false));
                /*if(seed.getPersons().contains(myPersonId)) {
                    editButton.setVisibility(View.VISIBLE);
                    infoButton.setVisibility(View.GONE);
                } else {
                    editButton.setVisibility(View.GONE);
                    infoButton.setVisibility(View.VISIBLE);
                }*/
                break;
            case 2:
            case 3:
            case 4:
                callButton.setVisibility(View.GONE);
                videoButton.setVisibility(View.GONE);
//                editButton.setVisibility(View.VISIBLE);
//                infoButton.setVisibility(View.GONE);
//                add.setVisibility(View.VISIBLE);
//                add.setImageResource(R.drawable.ic_action_add_group);
                break;
        }

        View[] gridLevelButtons = new View[5];
        gridLevelButtons[0] = mainView.findViewById(R.id.gridLevelButton0);
        gridLevelButtons[1] = mainView.findViewById(R.id.gridLevelButton1);
        gridLevelButtons[2] = null; //Skip this level
        gridLevelButtons[3] = null; //Skip this level
        gridLevelButtons[4] = mainView.findViewById(R.id.gridLevelButton4);

        for (int i = 0; i < gridLevelButtons.length; i++) {
            if(gridLevelButtons[i] == null) continue;

            float alpha = (mCurrentGridRadius == i) ? 1 : alphaStrong;
            gridLevelButtons[i].setAlpha(alpha);
            final int ii = i;
            gridLevelButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCurrentGridRadius != ii) {
                        mCurrentGridRadius = ii;
                        flowerGrid.setGridRadius(mCurrentGridRadius);
                        resetGridView(gridView);
                        initFragmentView();
                    }
                }
            });
        }

         //TODO options
//      (MainActivity) mActivity).reloadGrid(newGridRadius);

        //Restart the activity with the new parameters
//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.putExtra(ARG_SELECTED_GRID_RADIUS, newGridRadius);
//        startActivity(intent);
//        finish();
    }

    private void showRefreshButton() {
        final RelativeLayout gridView = (RelativeLayout) getView().findViewById(R.id.gridLayout);
        resetGridView(gridView);

        //Stop the loading progress
        final View progressView = getView().findViewById(R.id.grid_progress);
        progressView.setVisibility(View.GONE);
        final View refreshView = getView().findViewById(R.id.grid_refresh);
        refreshView.setVisibility(View.VISIBLE);
        refreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView.setVisibility(View.GONE);
                loadCommunity(mCurrentCommunityId);
            }
        });
    }

    //Remove all the elements from the view and init again
    private void resetGridView(ViewGroup gridView) {
        gridView.removeAllViews();

        View view = new Space(mActivity);
        view.setId(R.id.centerLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        gridView.addView(view, params);

        //getView().invalidate();
    }

    private View.OnTouchListener generateGridNodeOnTouchListener(final RelativeLayout gridView, final Grid grid) {
        //Gird node listener restricted to the node's circular area.
        View.OnTouchListener gridNodeTouchListener = new View.OnTouchListener() {

            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float xPoint = event.getX();
                        float yPoint = event.getY();
                        //Hex hex = grid.pixelToHex(event.getX(), event.getY()); //This can work on the RelativeLayout grid area
                        boolean isPointOutOfCircle = (grid.centerOffsetX -xPoint)*(grid.centerOffsetX -xPoint) + (grid.centerOffsetY -yPoint)*(grid.centerOffsetY -yPoint) > grid.hexWidth * grid.hexHeight / 4;

                        if (isPointOutOfCircle) return false;
                        else v.setSelected(true);
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_SCROLL:
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setSelected(false);
                        OnGridNodeClick(v, gridView);
                        break;
                }
                return true;
            }
        };

        return gridNodeTouchListener;
    }

    private View.OnClickListener generateActionButtonOnClickListener(final String displayName, final String sessionId, final boolean isCameraMuted) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((OnGridFragmentInteractionListener) mActivity).onActionButtonClicked(displayName, sessionId, isCameraMuted);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void addNodeViewToGridView(int width, int height, View nodeView, RelativeLayout gridView, Cube cube, Grid grid) {
        //Add to view
        RelativeLayout.LayoutParams params = null;

        //Set coordinates
        Point p;
        switch (grid.shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                p = grid.hexToPixel(cube.toHex());
                params = new RelativeLayout.LayoutParams(width, height);
                params.leftMargin = -width/2 + p.x;
                params.topMargin = -height/2 + p.y;
                break;
            case RECTANGLE:
                p = grid.hexToPixel(cube.cubeToOddRHex());
                params = new RelativeLayout.LayoutParams(width, height);
                params.leftMargin = -grid.hexWidth * grid.gridRadius -grid.centerOffsetX + p.x;
                params.topMargin = (int) (-1.5 * grid.hexRadius * grid.gridRadius -grid.centerOffsetY + p.y);
                break;
        }

        params.addRule(RelativeLayout.RIGHT_OF, R.id.centerLayout);
        params.addRule(RelativeLayout.BELOW, R.id.centerLayout);
        gridView.addView(nodeView, params);
    }

    private void OnGridNodeClick(View view, RelativeLayout gridView) {
        if(view instanceof PersonNode) {
            Cube personCube = ((PersonNode) view).getCube();
            if(personCube == null) return;

            Cube centralPersonCube = FlowerGrid.getCentralPersonCube(mCurrentSeedCube);//the central cube in the selected seed
            mCurrentPersonCube = personCube.add(centralPersonCube); //add the centralPersonCube to shift accordingly to the current seed
            mCurrentGridRadius = 0;
            mFlowerGrid.setGridRadius(mCurrentGridRadius);
        } else if(view instanceof SeedNode) {
            Cube seedCube = ((SeedNode) view).getCube();
            if(seedCube == null) return;

            Cube centerCube = FlowerGrid.getCentralPersonCube(seedCube);
            mCurrentPersonCube = centerCube.add(mCurrentPersonCube);
            mCurrentSeedCube = seedCube.add(mCurrentSeedCube);
            mCurrentGridRadius = 1;
            mFlowerGrid.setGridRadius(mCurrentGridRadius);
        }

        resetGridView(gridView);
        initFragmentView();
    }

    private void loadCommunity(final String communityId) {
        try {
            //Stop the loading progress
            View progressView = getView().findViewById(R.id.grid_progress);
            progressView.setVisibility(View.VISIBLE);

            ApiManager.getInstance().getCommunity(
                    new Response.Listener<Community>() {
                        @Override
                        public void onResponse(Community response) {
                            Log.v(TAG, "Community instance loaded.");

                            if (response != null) {
                                community = response;
                                loadCommunityData(communityId);
                            } else {
                                Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_LONG).show();
                                showRefreshButton();
                            }
                        }
                    },
                    createReqErrorListener(),
                    communityId,
                    ApiManager.REQUEST_TAG_MAIN);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_LONG).show();
            showRefreshButton();
        }
    }

    private void loadCommunityData(String communityId) {
        try {
            ApiManager.getInstance().getCommunityData(
                    new Response.Listener<CommunityData>() {
                        @Override
                        public void onResponse(CommunityData response) {
                            Log.v(TAG, "Community data loaded.");

                            if (response != null) {
                                Map<String, Flower> flowers = response.getAllFlowers();
                                Flower flower = flowers.get(community.getGroups().get(0));//Get the only flower, now there is only one flower supported
                                Map<String, Seed> seeds = response.getAllSeeds();
                                Map<String, Person> persons = response.getAllPersons();

                                //Init the flower model with its grid data
                                mFlowerGrid = new FlowerGrid(mCurrentGridRadius, mCurrentHexRadius, Grid.Shape.FLOWER_OF_LIFE, isOrientationFlatTop, persons, seeds, flower);

                                //Stop the loading progress
                                View progressView = getView().findViewById(R.id.grid_progress);
                                progressView.setVisibility(View.GONE);

                                initFragmentView();
                            } else {
                                Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_LONG).show();
                                showRefreshButton();
                            }
                        }
                    },
                    createReqErrorListener(),
                    communityId,
                    ApiManager.REQUEST_TAG_MAIN
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_LONG).show();
            showRefreshButton();
        }
    }

    private Response.ErrorListener createReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Grid data failed to load");
                error.printStackTrace();
                if(mActivity != null) {
                    Toast.makeText(mActivity, R.string.connection_error, Toast.LENGTH_LONG).show();
                    showRefreshButton();
                }
            }
        };
    }

    public interface OnGridFragmentInteractionListener {
        void onActionButtonClicked(String displayName, String sessionId, boolean isCameraMuted);
        void onGridChanged(Cube personCube, Cube seedCube, int gridRadius, FlowerGrid mFlowerGrid);
        void onTitleAndBgChanged(String title, String subtitle, String url);
    }

    /* Getters and Setters ***********************/

    public FlowerGrid getFlowerGrid() {
        return mFlowerGrid;
    }

    public Community getCommunity() {
        return community;
    }

    public Cube getCurrentPersonCube() {
        return mCurrentPersonCube;
    }

    public Cube getCurrentSeedCube() {
        return mCurrentSeedCube;
    }

    public int getCurrentGridRadius() {
        return mCurrentGridRadius;
    }
}