package net.omplanet.starwheel.ui.view;

import android.content.Context;
import android.view.View;

import net.omplanet.starwheel.model.hexagonalgrids.Cube;

public class SeedView extends View implements SeedNode {
    private Cube mCube; //Hold the node coordinates on the grid

    public SeedView(Context context) {
        super(context);
    }

    @Override
    public Cube getCube() {
        return mCube;
    }

    @Override
    public void setCube(Cube cube) {
        mCube = cube;
    }
}
