package cn.treeh.Utils;

import java.util.Map;

public class GTFrecord {
    public int begin, end;
    public Map<String, String> info;
    public GTFrecord(int begin, int end, Map<String, String> info){
        this.begin = begin;
        this.end = end;
        this.info = info;
    }
}
