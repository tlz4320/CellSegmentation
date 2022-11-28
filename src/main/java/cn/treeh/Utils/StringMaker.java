package cn.treeh.Utils;

public class StringMaker {
    public static String makeString(String[] sp){
        StringBuilder builder = new StringBuilder();
        if(sp == null || sp.length == 0)
            return builder.toString();
        boolean first = true;
        for(String i : sp) {
            if(first) {
                builder.append(i);
                first = false;
            }
            else
                builder.append('\t').append(i);
        }
        return builder.toString();

    }
}
