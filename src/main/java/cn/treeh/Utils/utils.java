package cn.treeh.Utils;

import java.lang.reflect.Array;

public class utils {

    public static int[] baseMap = new int[256];
    static{
        baseMap['T'] = 1;
        baseMap['t'] = 1;
        baseMap['C'] = 2;
        baseMap['c'] = 2;
        baseMap['G'] = 3;
        baseMap['g'] = 3;
    }
    public static <T> T[] rep(T o, int size) {
        try {
            T[] res = (T[])Array.newInstance(o.getClass(), size);
            for(int i = 0; i < size; i++)
                res[i] = o;
            return res;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
