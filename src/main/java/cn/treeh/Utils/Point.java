package cn.treeh.Utils;

public class Point {
    public int x, y;
    public double[] v;
    public Point() {
    }
    public Point(int _x, int _y, double[] _v){
        x = _x;
        y = _y;
        v = _v;
    }
    public Point(int _x, int _y) {
        x = _x;
        y = _y;
    }

    @Override
    public int hashCode() {
        return (x << 10 + y) / 17 + x;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            return ((Point) obj).x == x && ((Point) obj).y == y;
        }
        return false;
    }
}
