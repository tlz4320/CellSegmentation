package cn.treeh.Utils;

public class Region {
    public int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
    public Region(int x, int y){
        maxX = minX = x;
        maxY = minY = y;
    }
    public Region(int _minX, int _maxX, int _minY, int _maxY){
        minX = _minX;
        maxX = _maxX;
        minY = _minY;
        maxY = _maxY;
    }
    public Region(){} //以上是三种方式对region的初始化 //contians方法判断region是否包括了那一个点，T表示包括
    public boolean contains(int x, int y){
        return maxX >= x && minX <= x && maxY >= y && minY <= y;
    } //返回T/F
    public boolean containsX(int x){
        return maxX >= x && minX <= x;
    }
    public boolean containsY(int y){
        return maxY >= y && minY <= y;
    }

    @Override
    //比较两个对象是否相同
    public boolean equals(Object o){ //equals() 方法用于判断 Number 对象与方法的参数进是否相等
        if(o instanceof Region){ //它的作用是测试它左边的对象是否是它右边的类的实例，返回 boolean 的数据类型 判断是否是region
            Region or = (Region) o; //将对象o强制转化region类
            return or.minX == minX && or.maxX == maxX && or.minY == minY && or.maxY == maxY;
        }
        return false;
    }
    public double square(){
        return width() * height();
    }
    public boolean overlap(Region o){
        return o.maxY >= minY && maxY >= o.minY && o.maxX >= minX && maxX >= o.minX;
    }
    public void recordX(int x){
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
    }
    public void recordY(int y){
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
    }
    public int width(){
        return maxX - minX + 1;
    }
    public int height(){
        return maxY - minY + 1;
    }

    @Override
    public String toString() {
        return minX + "," + minY + "," + maxX + "," + maxY;
    }
}
