package net.omplanet.starwheel.model.hexagonalgrids;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A square grid that can cover the range of axial grid coordinates.
 * Storage for Hex nodes with column:q and row:r.
 * Sliding the rows to the left, and using variable sized rows to save space.
 * The access formula is array[r][q + r/2] if the grid is arranged in rectangular shape.
 * The access formula is array[r + radius][q + radius + min(0, r)] if arranged in hexagonal shape.
 */
public class StorageMap {
    private static final String TAG = StorageMap.class.getName();

    public static Cube[] generateNodes(Grid grid, boolean spiral) throws ArrayIndexOutOfBoundsException {
        return generateNodes(new Cube(0,0,0), grid.gridRadius, grid.shape, spiral);
    }

     public static Cube[] generateNodes(Cube center, int gridRadius, Grid.Shape shape, boolean spiral) throws ArrayIndexOutOfBoundsException {
        Cube[] nodes = null;

        //Init nodes
        switch (shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                //generateHexagonalShape(gridRadius, shape, true)
                if(spiral) {
                    nodes = generateHexagonalSpiral(center, gridRadius, true, true, shape);
                } else {
                    nodes = generateHexagonalShape(gridRadius, shape);
                }
                break;

            case RECTANGLE:
                //generateRectangleShape(gridRadius, shape);
                int minQ=0;
                int maxQ=gridRadius*2;
                int minR=0;
                int maxR=gridRadius*2;

                nodes = new Cube[Grid.getNumberOfNodesInGrid(gridRadius, shape)];
                int i = 0;

                for (int q = minQ; q <= maxQ; q++) {
                    for (int r = -minR; r <= maxR; r++) {
                        nodes[i++] = new Hex(q,r).oddRHexToCube(); //conversion to cube is different for oddR coordinates
                    }
                }
                break;
        }

        return nodes;
    }

    private static Cube[] generateHexagonalShape(int gridRadius, Grid.Shape shape) {
        Cube[] nodes = new Cube[Grid.getNumberOfNodesInGrid(gridRadius, shape)];

        int i = 0;
        for (int x = -gridRadius; x <= gridRadius; x++) {
            for (int y = -gridRadius; y <= gridRadius; y++) {
                int z = -x-y;
                if (Math.abs(x) <= gridRadius && Math.abs(y) <= gridRadius && Math.abs(z) <= gridRadius) {
                    nodes[i++] = new Cube(x, y, z);
                }
            }
        }

        return nodes;
    }

        /**
         * The spiral ring algorithm as described in the article
         * @param center
         * @param spiral spiral or ring
         * @return
         */
    private static Cube[] generateHexagonalSpiral(Cube center, int spiralRadius, boolean spiral, boolean clockwise, Grid.Shape shape) {
        Cube[] nodes = new Cube[Grid.getNumberOfNodesInGrid(spiralRadius, shape)];

        List<Cube> nodesList = new ArrayList<Cube>();
        nodesList.add(center);

        for (int k = spiral ? 1 : spiralRadius; k <= spiralRadius; k++) {
            Cube H = Cube.directionsClockwise[0].scale(k);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < k; j++) {
                    nodesList.add(H.add(center));//shift relatively, from (0,0,0) to the given center
                    if(clockwise) H = H.subtract(Cube.directionsClockwise[(6+i-1)%6]);
                    else H = H.add(Cube.directionsClockwise[i]);
                }
            }
        }

        return nodesList.toArray(nodes);
    }

    public static Object getSpiralMapObjectByHex(Hex hex, int mRadius, Object[] objects) {
        try {
            if(mRadius <= 1) {
                if(hex.getQ() == 0 && hex.getR() == 0) {
                    return objects[0];
                } else for(int i=0; i<6; i++) {
                    if(Cube.directionsClockwise[i].equals(hex.toCube())) return objects[i];
                }
            } else {
                //TODO
                return null;
            }
        } catch (Exception e) {
            Log.d(TAG, "getObjectByCoordinate(): IndexOutOfBound, the array element is not found for " + hex);
        }

        return null;

    }

    //Slide the rows to the left, and use variable sized rows.
    public static Object getSquareMapObjectByHex(Hex hex, int mRadius, Grid.Shape shape, Object[][] mSquareMapObjects) {
        try {
            int q = hex.getQ();
            int r = hex.getR();

            switch (shape) {
                case HEXAGON:
                case FLOWER_OF_LIFE:
                    return mSquareMapObjects[r + mRadius][q + mRadius + Math.min(0, r)];
                case RECTANGLE:
                    return mSquareMapObjects[r][q];
            }
        } catch (Exception e) {
            Log.d(TAG, "getObjectByCoordinate(): IndexOutOfBound, the array element is not found for " + hex);
        }

        return null;
    }
}
