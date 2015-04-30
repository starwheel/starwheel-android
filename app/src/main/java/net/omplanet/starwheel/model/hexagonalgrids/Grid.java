package net.omplanet.starwheel.model.hexagonalgrids;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
        * *   * *   *
       *   * *   * *
        * *   * *   *
       *   * *   * *
        * *   * *   *
 * A grid of hex nodes with axial coordinates.
 * http://www.redblobgames.com/grids/hexagons/
 */
 public class Grid {
    public enum Shape {
        RECTANGLE,
        HEXAGON, //hexagons arranged like a bee hive
        FLOWER_OF_LIFE //Similar to hexagons, but the radius of the hexes is double and arranged like a flower of life
    }

    //Grid properties
    public int gridRadius; //The gridRadius of the grid - the count of rings around the central node
    public final Shape shape; //The shape of the grid
    public final boolean orientationIsFlatTop; //If true - the single cell hexagon is flat top, but the tile of hexagons is pointy top

    //Grid node dimension properties
    public int hexRadius; //The radius of the single node in grid
    public int hexWidth; //The width of the single hexagon node
    public int hexHeight; //The height of the single hexagon node
    public int centerOffsetX; //Relative center coordinate within one node
    public int centerOffsetY; //Relative center coordinate within one node

    /**
     * Construing a Grid with a set of cubes, hexRadius, and shape
     * @param gridRadius The count of rings around the central node
     * @param hexRadius The gridRadius of the hexagon in pixels
     * @param shape The shape of the hexagon
     */
    public Grid(int gridRadius, int hexRadius, Shape shape, boolean orientationIsFlatTop) {
        this.gridRadius = gridRadius;
        this.hexRadius = hexRadius;
        this.shape = shape;
        this.orientationIsFlatTop = orientationIsFlatTop;

        setHexRadius(hexRadius);
    }

    public int getHexRadius() {
        return hexRadius;
    }

    public void setHexRadius(int hexRadius) {
        if(hexRadius == 0) return;

        this.hexRadius = hexRadius;
        //Init derived node properties
        if(orientationIsFlatTop) {
            hexWidth = 2 * hexRadius;
            hexHeight = (int) (Math.sqrt(3) * hexRadius);
        } else {
            hexWidth = (int) (Math.sqrt(3) * hexRadius);
            hexHeight = 2 * hexRadius;
        }
        centerOffsetX = hexWidth /2;
        centerOffsetY = hexHeight /2;
    }

    public void setGridRadius(int gridRadius) {
        this.gridRadius = gridRadius;
    }

    public int getGridRadius() {
        return gridRadius;
    }

    public Point hexToPixel(Hex hex) {
        int x = 0;
        int y = 0;

        switch (shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    x = (int) (hexRadius * 1.5 * hex.getQ());
                    y = (int) (hexHeight * (hex.getR() + 0.5 * hex.getQ()));
                } else {
                    x = (int) (hexWidth * (hex.getQ() + 0.5 * hex.getR()));
                    y = (int) (hexRadius * 1.5 * hex.getR());
                }
                break;
            case RECTANGLE:
                //oddR alignment
                x = (int) (hexWidth * hex.getQ() + 0.5 * hexWidth * (hex.getR()%2));
                y = (int) (hexRadius * 1.5 * hex.getR());
                break;
        }

        return new Point(x, y);
    }

    public Hex pixelToHex(float x, float y) {
        switch (shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    float q = (x * (2/3)) / hexRadius;
                    float r = (float) (-x / 3 + y * Math.sqrt(3)/3) / hexRadius;
                    return new Hex(q, r);
                } else {
                    float q = (float) ((x * Math.sqrt(3)/3) - y * (1/3)) / hexRadius;
                    float r = (y * (2/3)) / hexRadius;
                    return new Hex(q, r);
                }

            default:
                return null;
        }
    }

    /**
     * The spiral ring algorithm as described in the article
     * @param center
     * @param spiral spiral or ring
     * @return
     */
    private static List<Cube> generateSpiral(Cube center, int spiralRadius, boolean spiral, boolean clockwise) {
        List<Cube> results = new ArrayList<Cube>();
        results.add(new Cube(0, 0, 0));

        for (int k = spiral ? 1 : spiralRadius; k <= spiralRadius; k++) {
            Cube H = Cube.directionsClockwise[0].scale(k);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < k; j++) {
                    results.add(H);
                    if(clockwise) H = H.subtract(Cube.directionsClockwise[(6+i-1)%6]);
                    else H = H.add(Cube.directionsClockwise[i]);
                }
            }
        }

        return results;
    }

    /**
     * @return Number of hexagons inside of a hex or oddR rectangle shaped grid with the given gridRadius
     */
    public static int getNumberOfNodesInGrid(int radius, Shape shape) {
        switch (shape) {
            case HEXAGON:
            case FLOWER_OF_LIFE:
                return (int) (3 * Math.pow(radius+1, 2) - 3 * (radius +1) + 1);
            case RECTANGLE:
                return (radius * 2 + 1) * (radius * 2 + 1);
        };

        return 0;
    }

    public static int getHexRadius(int gridRadius, int containerViewWidth, Shape shape, boolean orientationIsFlatTop) {
        switch (shape) {
            case HEXAGON:
                if(orientationIsFlatTop) {
                    return (int) (containerViewWidth / ((2*gridRadius + 1) * 1.5 + 0.5) );
                } else {
                    return (int) (containerViewWidth / ((2*gridRadius + 1) * (Math.sqrt(3))));
                }
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    return (int) (containerViewWidth / ((2*gridRadius + 1) * 1.5 + 2.5) );
                } else {
                    return (int) (containerViewWidth / ((2*gridRadius + 2) * (Math.sqrt(3))));
                }
            case RECTANGLE:
                //TODO return int hexRadius = (int) (containerViewWidth / ((2*gridRadius + 1) * (Math.sqrt(3))));
        }

        return 0;
    }

    public static int getGridWidth(int gridRadius, int hexRadius, Shape shape, boolean orientationIsFlatTop) {
        switch (shape) {
            case HEXAGON:
                if(orientationIsFlatTop) {
                    return (int) (hexRadius * ((2*gridRadius + 1) * 1.5 + 0.5));
                } else {
                    return (int) ((2*gridRadius + 1) * Math.sqrt(3) * hexRadius);
                }
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    return (int) (hexRadius * ((2*gridRadius + 1) * 1.5 + 2.5));
                } else {
                    return (int) ((2 * gridRadius + 2) * Math.sqrt(3) * hexRadius);
                }
            case RECTANGLE:
                return 0; //TODO
        };

        return 0;
    }

    public static int getGridHeight(int gridRadius, int hexRadius, Shape shape, boolean orientationIsFlatTop) {
        switch (shape) {
            case HEXAGON:
                if(orientationIsFlatTop) {
                    return (int) ((2*gridRadius + 1) * Math.sqrt(3) * hexRadius);
                } else {
                    return (int) (hexRadius * ((2*gridRadius + 1) * 1.5 + 0.5));
                }
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    return (int) ((2 * gridRadius + 2) * Math.sqrt(3) * hexRadius);
                } else {
                    return (int) (hexRadius * ((2*gridRadius + 1) * 1.5 + 2.5));
                }
            case RECTANGLE:
                return 0; //TODO
        };

        return 0;
    }

    public static double getGridWidthDividedToHeight(int radius, Shape shape, boolean orientationIsFlatTop) {
        switch (shape) {
            case HEXAGON:
                if(orientationIsFlatTop) {
                    return ((2*radius + 1) * 1.5 + 0.5) / ((2*radius + 1) * Math.sqrt(3));
                } else {
                    return ((2*radius + 1) * Math.sqrt(3)) / ((2*radius + 1) * 1.5 + 0.5);
                }
            case FLOWER_OF_LIFE:
                if(orientationIsFlatTop) {
                    return ((2*radius + 1) * 1.5 + 2.5) / ((2*radius + 2) * Math.sqrt(3));
                } else {
                    return ((2*radius + 2) * Math.sqrt(3)) / ((2*radius + 1) * 1.5 + 2.5);
                }
            case RECTANGLE:
                return 0; //TODO
        };

        return 0;
    }
}
