package net.omplanet.starwheel.model.hexagonalgrids;

/**
 * Cube using 3-vector for the coordinates (x, y, z)
 */
public class Cube {
    private int x;
    private int y;
    private int z;

    public static Cube[] directionsClockwise = {new Cube(1, 0, -1), new Cube(1, -1, 0), new Cube(0, -1, 1), new Cube(-1, 0, 1), new Cube(-1, 1, 0), new Cube(0, 1, -1)};
    public static Cube[] directionsAntiClockwise = {new Cube(0, 1, -1), new Cube(-1, 1, 0), new Cube(-1, 0, 1), new Cube(0, -1, 1), new Cube(1, -1, 0), new Cube(1, 0, -1)};

    public Cube (String coordinates) {
        this.x = new Integer(coordinates.split(":")[0]);
        this.y = new Integer(coordinates.split(":")[1]);
        this.z = new Integer(coordinates.split(":")[2]);
    }

    public Cube(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Cube(float x, float y, float z) {
        int rx = Math.round(x);
        int ry = Math.round(y);
        int rz = Math.round(z);

        float x_diff = Math.abs(rx - x);
        float y_diff = Math.abs(ry - y);
        float z_diff = Math.abs(rz - z);

        if (x_diff > y_diff && x_diff > z_diff)
            rx = -ry-rz;
        else if (y_diff > z_diff)
            ry = -rx-rz;
        else
            rz = -rx-ry;

        this.x = rx;
        this.y = ry;
        this.z = rz;
    }

    public int[] getCoordinates() {
        return new int[]{x, y, z};
    }
    public Hex  toHex() {
        return new Hex(x, z);
    }

    public Hex cubeToOddRHex() {
        int q = x + (z - (z&1)) / 2;
        int r = z;

        return new Hex(q, r);
    }

    public int length() {
        int len = 0;

        for(int v : getCoordinates())
        if (Math.abs(v) > len) {
            len = Math.abs(v);
        }

        return len;
    }

    public boolean equals(Cube other) {
        return (x == other.x && y == other.y);
    }

    public Cube clone() {
        return new Cube(x, y, z);
    }

    public Cube subtract(Cube other) {
        return new Cube(x - other.x, y - other.y, z - other.z);
    }

    public Cube add(Cube other) {
        return new Cube(x + other.x, y + other.y, z + other.z);
    }

    public Cube scale(int k) {
        return new Cube(k * x, k * y, k * z);
    }

    public String toString() {
        return x + ":" + y + ":" + z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
