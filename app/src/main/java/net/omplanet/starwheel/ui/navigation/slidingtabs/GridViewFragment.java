package net.omplanet.starwheel.ui.navigation.slidingtabs;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Toast;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.api.DemoObjects;
import net.omplanet.starwheel.hexagonalgrids.Cube;
import net.omplanet.starwheel.hexagonalgrids.Grid;
import net.omplanet.starwheel.hexagonalgrids.Hex;
import net.omplanet.starwheel.hexagonalgrids.StorageMap;
import net.omplanet.starwheel.ui.view.CircleImageView;

/**
 * A fragment which contains the main hexagonal grid.
 */
public class GridViewFragment extends Fragment {
    public final String TAG = getClass().getName();

    private int mCurrentRadius;
    private int containerViewWidth;
    private int containerViewHeight;

    //Grid properties
    public static final Grid.Shape mShape = Grid.Shape.HEXAGON_POINTY_TOP;
    public final static String ARG_RADIUS = "radius";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous grid parameters set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            mCurrentRadius = savedInstanceState.getInt(ARG_RADIUS);
        } else if (getArguments() != null) {
            // Set grid parameters based on argument passed in
            mCurrentRadius = getArguments().getInt(ARG_RADIUS);
        } else {
            Log.d(TAG, "Should not have reached here.");
        }

        final View view = inflater.inflate(R.layout.fragment_grid_view, container, false);

        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this); // API version 16 and higher.
                containerViewWidth = container.getMeasuredWidth();
                containerViewHeight = container.getMeasuredHeight();
                Log.d("Grid Fragment:------", "window width: " + containerViewWidth + "   window height: " + containerViewHeight);

                initFragmentLayout();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_RADIUS, mCurrentRadius);
    }

    public void initFragmentLayout() {
        RelativeLayout gridView = (RelativeLayout) getView().findViewById(R.id.gridLayout);

        // Init grid view
        int scale = setGridDimensions(gridView, containerViewWidth, containerViewHeight, mCurrentRadius, mShape);
        setGridView(gridView, mCurrentRadius, scale, mShape);

        // Init grid action buttons
        int size = (containerViewWidth > containerViewHeight) ? containerViewHeight/6 : containerViewWidth/6;
        setGridButtons(getView(), gridView, size, mCurrentRadius);
    }

    private int setGridDimensions(View gridView, int containerViewWidth, int containerViewHeight, int radius, Grid.Shape shape) {
        // Gets the layout params that will allow to resize the layout
        ViewGroup.LayoutParams params = gridView.getLayoutParams();

        //If the height is smaller then the width, use smaller width calculated according to the ratio of the shape .
        switch (shape) {
            case HEXAGON_POINTY_TOP:
                //int maxWidthByHeight = (int) (2 * containerViewHeight / Math.sqrt(3));
                int maxWidthByHeight = (int) (containerViewHeight * Grid.getGridWidthDividedToHeight(radius));
                if(containerViewWidth > maxWidthByHeight) containerViewWidth = maxWidthByHeight;
                break;
            case RECTANGLE:
                if(containerViewWidth > containerViewHeight) containerViewWidth = containerViewHeight;
                break;
        }

        // Calculate the padding
//        int horizontalPadding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
//        //int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontalPaddingInDp, getResources().getDisplayMetrics());
//        fragmentWidth -= 2 * horizontalPadding;

        // Calculate the scale: the radius of single node.
        int scale = (int) (containerViewWidth / ((2*radius + 1) * (Math.sqrt(3))));

        // Changes the height and width of the grid to the specified *pixels*
        params.width = Grid.getGridWidth(radius, scale, shape);
        params.height = Grid.getGridHeight(radius, scale, shape);

        return scale;
    }

    private Grid setGridView(RelativeLayout gridView, int radius, int scale, Grid.Shape shape) {
        //Init group elements
        //TODO setGridNodes(radius-1, scale*2, shape);

        //Init node elements
        return setGridNodes(gridView, radius, scale, shape);
    }

    private void setGridButtons(View mainView, final ViewGroup gridView, int size, final int radius) {
        View zoomOutButton = mainView.findViewById(R.id.zoomOutButton);
        ViewGroup.LayoutParams params = zoomOutButton.getLayoutParams();
        params.width = size;
        params.height = size;

        View zoomInButton = mainView.findViewById(R.id.zoomInButton);
        params = zoomInButton.getLayoutParams();
        params.width = size;
        params.height = size;

        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newRadius = radius+1;
                if(newRadius > 12) return;
                mCurrentRadius = newRadius;

                reloadView(gridView);

                //TODO options
//                ((MainActivity) getActivity()).reloadGrid(newRadius);

                //Restart the activity with the new parameters
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                intent.putExtra("GRID_RADIUS", newRadius);
//                startActivity(intent);
//                finish();
            }
        });

        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newRadius = radius-1;
                if(newRadius < 0) return;
                mCurrentRadius = newRadius;

                reloadView(gridView);

                //TODO options
//                ((MainActivity) getActivity()).reloadGrid(newRadius);

                //Restart the activity with the new parameters
//                Intent intent = new Intent(getActivity(), MainActivity.class);
//                intent.putExtra("GRID_RADIUS", newRadius);
//                startActivity(intent);
//                finish();
            }
        });
    }

    private void reloadView(ViewGroup gridView) {
        //Remove all the elements from the view and init again
        gridView.removeAllViews();

        View view = new Space(getActivity());
        view.setId(R.id.centerLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        gridView.addView(view, params);

        initFragmentLayout();

        getView().invalidate();
    }

    private Grid setGridNodes(RelativeLayout gridView, int radius, int scale, Grid.Shape shape) {
        try {
            StorageMap storageMap = new StorageMap(radius, shape, DemoObjects.squareMap);
            final Grid grid = new Grid(radius, scale, shape);

            //Gird node listener restricted to the node's circular area.
            View.OnTouchListener gridNodeTouchListener = new View.OnTouchListener() {

                @Override
                public boolean onTouch(final View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            float xPoint = event.getX();
                            float yPoint = event.getY();
                            //Hex hex = grid.pixelToHex(event.getX(), event.getY()); //This can work on the RelativeLayout grid area
                            boolean isPointOutOfCircle = (grid.centerOffsetX -xPoint)*(grid.centerOffsetX -xPoint) + (grid.centerOffsetY -yPoint)*(grid.centerOffsetY -yPoint) > grid.width * grid.width / 4;

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
                            CircleImageView nodeView = (CircleImageView) v;
                            OnGridHexClick(nodeView.getHex());
                            break;
                    }
                    return true;
                }
            };

            for(Cube cube : grid.nodes) {
                Hex hex = null;
                switch (shape) {
                    case HEXAGON_POINTY_TOP:
                        hex = cube.toHex();
                        break;
                    case RECTANGLE:
                        hex = cube.cubeToOddRHex();
                        break;
                }

                CircleImageView nodeView = new CircleImageView(getActivity());
                Integer pic = (Integer) storageMap.getObjectByCoordinate(hex.getQ(), hex.getR());
                if(pic == null) {
                    nodeView.setHex(hex);
                    nodeView.setOnTouchListener(gridNodeTouchListener);
//                    view.setBackgroundResource(R.drawable.ring_bg);
                    nodeView.setImageResource(R.drawable.empty_image);
                } else {
                    nodeView = new CircleImageView(getActivity());
                    //view.setBackgroundResource(R.drawable.hexagon);
                    nodeView.setOnTouchListener(gridNodeTouchListener);
                    nodeView.setHex(hex);
                    if(pic != 0) {
                        nodeView.setImageResource(pic);
                    } else {
                        nodeView.setImageResource(R.drawable.no_profile_image);
                    }
                }
                addViewToLayout(gridView, nodeView, hex, grid);
            }

            return grid;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Sorry, there was a problem initializing the application.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return null;
    }

    private void addViewToLayout(RelativeLayout gridView, View nodeView, Hex hex, Grid grid) {
        //Add to view
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(grid.width, grid.height);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.centerLayout);
        params.addRule(RelativeLayout.BELOW, R.id.centerLayout);
        gridView.addView(nodeView, params);

        //Set coordinates
        Point p = grid.hexToPixel(hex);
        switch (grid.shape) {
            case HEXAGON_POINTY_TOP:
                params.leftMargin = -grid.centerOffsetX + p.x;
                params.topMargin = -grid.centerOffsetY + p.y;
                break;
            case RECTANGLE:
                params.leftMargin = -grid.width * grid.radius -grid.centerOffsetX + p.x;
                params.topMargin = (int) (-1.5 * grid.scale * grid.radius -grid.centerOffsetY + p.y);
                break;
        }
    }

    private void OnGridHexClick(Hex hex) {
        Toast.makeText(getActivity(), "OnGridHexClick: " + hex, Toast.LENGTH_SHORT).show();
    }
}