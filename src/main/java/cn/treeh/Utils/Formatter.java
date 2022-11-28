package cn.treeh.Utils;

public class Formatter {
    public static double[] toDoubleArray(String[] sp, int begin){
        if(sp == null || sp.length < begin)
            return null;
        return toDoubleArray(sp, begin, sp.length - 1);
    }
    public static int[] toIntegerArray(String[] sp, int begin){
        if(sp == null || sp.length < begin)
            return null;
        return toIntegerArray(sp, begin, sp.length - 1);
    }
    public static int[] toIntegerArray(String[] sp, int begin, int end){
        int[] res = new int[end - begin + 1];
        int index = begin;
        while(index <= end){
            res[index - begin] = Integer.parseInt(sp[index]);
            index++;
        }
        return res;
    }
    public static double[] toDoubleArray(String[] sp, int begin, int end){
        double[] res = new double[end - begin + 1];
        int index = begin;
        while(index <= end){
            res[index - begin] = Double.parseDouble(sp[index]);
            index++;
        }
        return res;
    }
}
