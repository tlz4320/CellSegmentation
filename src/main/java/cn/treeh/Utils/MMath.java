package cn.treeh.Utils;

public class MMath {
    public static int ceil_floor(int min, int max, int v){
        return Math.max(min, Math.min(max, v));
    }
}
