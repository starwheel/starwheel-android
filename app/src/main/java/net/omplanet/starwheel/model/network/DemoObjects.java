package net.omplanet.starwheel.model.network;

/**
 * Demo contents.
 */
public class DemoObjects {
    //int size = mRadius*2+1;
    //squareMap = new Integer[size][size];

    public static Integer[][] squareMap = new Integer[][] {
            {1, 1},
            {1, 1, 1},
            {1, 1}};

    public static Object[][] squareMapOfMap = new Integer[][][][] {
            {squareMap, squareMap},
            {squareMap, squareMap, squareMap},
            {squareMap, squareMap}};

    /*public static Object[][] squareMap = new Integer[][] {
        {R.drawable.profile1, 0, R.drawable.profile2, R.drawable.profile3},
        {R.drawable.profile4, null, R.drawable.profile5, null, R.drawable.profile6},
        {R.drawable.profile7, R.drawable.profile8, R.drawable.profile9, R.drawable.profile10, R.drawable.profile11, R.drawable.profile12},
        {0, null, 0, null, 0, null, 0},
        {R.drawable.profile13, 0, 0, 0, 0, 0},
        {0, null, R.drawable.profile14, null, R.drawable.profile15},
        {0, 0, 0, 0}
    };

    public static Object[][] squareMap1 = new Integer[][] {
            {null, null, null, R.drawable.profile3},
            {null, null, null, null, R.drawable.profile6},
            {R.drawable.profile7, null, null, R.drawable.profile10, R.drawable.profile11, R.drawable.profile12},
            {null, null, null, null, 0, null, 0},
            {null, null, 0, 0, 0, 0},
            {0, null, R.drawable.profile14, null, R.drawable.profile15},
            {0, 0, 0, 0}
    };*/
}
