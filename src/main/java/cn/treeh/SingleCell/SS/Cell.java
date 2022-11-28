package cn.treeh.SingleCell.SS;

import cn.treeh.Utils.Point;
import cn.treeh.Utils.Region;
import org.opencv.core.Mat;

import java.util.LinkedList;

public class Cell {
    LinkedList<Point> surface;
    LinkedList<Point> innerSurface;
    int cellID;
    int innerCount = 0;
    int surfaceCount = 0;
    LinkedList<Point> final_surface;
    Point kernel; // 扩张中心
    Region cellRegion; // 细胞所在那个方块 一个20X20的坐标 ~400个点
    LinkedList<Point> destroy;
    double threshold;
    public Cell(LinkedList<Point> p, int _cellID){
        double x = 0;
        double y = 0;
        cellRegion = new Region();
        cellID = _cellID;
        for(Point point : p){
            x += point.x / (double)p.size();
            y += point.y / (double)p.size();
            cellRegion.recordY(point.y);
            cellRegion.recordX(point.x);
        }
        surface = new LinkedList<>();
        innerSurface = p;
        innerCount = innerSurface.size();
        kernel = new Point((int)x , (int)y);
    }
    public Region getCellRegion(){
        return cellRegion;
    }
    public boolean goodCell(){
        return cellRegion.square() > 25 && cellRegion.width() > 5 && cellRegion.height() > 5;
    }
    public boolean remove(Point p){
        return surface.remove(p);
    }
    public LinkedList<Point> getSurface(){
        return final_surface;
    }


    public void reCalculateThreshold(Mat original){
        double acc = 0, tmp;
        int sum = 0;
        for(int x = cellRegion.minX; x <= cellRegion.maxX; x++){
            for(int y = cellRegion.minY; y <= cellRegion.maxY; y++){
                tmp = original.get(x, y)[0];
                if(tmp != 0){
                    acc += original.get(x, y)[0];
                    sum++;
                }

            }
        }
        //prevent NA error
        threshold = acc / (sum == 0 ? 1 : sum);
    }
    public void resetThreshold(Mat original){//每一次阈值降低 要调用一次这个
        //让上一个阈值没找到的点再找一次
        reCalculateThreshold(original);
        innerCount = innerSurface.size();
    }
    public void resetThreshold(){//每一次阈值降低 要调用一次这个
        //让上一个阈值没找到的点再找一次
        final_surface = (LinkedList<Point>) innerSurface.clone();
        innerCount = innerSurface.size();
    }
    //每次扩张一圈 调用一次这个
    public void resetCell(){
        surfaceCount = surface.size();
    }
    //获得一个点 来扩张
    public Point getPoint(){
        innerCount--;
        if(innerCount >= 0){
            return innerSurface.pop();
        }
        surfaceCount--;
        if(surfaceCount >= 0){
            return surface.pop();
        }
        return null;
    }
    public void addPoint(Point surface_point, Point newPoint){
        surface.remove(surface_point);
        surface.add(newPoint);
    }
    public void addPoints(Point surface_point, LinkedList<Point> points, Mat original, double beta) { //往外扩张一个点，如果包含，就把这个点移除
        if (points == null || points.size() == 0) {//如果点没有扩张 检查是不是来自inner 是的话就不管了 不是的话加入tmp
            if (innerCount < 0)
                innerSurface.add(surface_point);
            return;
        }
        for (Point p : points) {
            if (threshold < beta * original.get(p.x, p.y)[0]) {
                cellRegion.recordY(p.y);
                cellRegion.recordX(p.x);
            }
            surface.add(p);
        }
    }
    public void addPoints(LinkedList<Point> points){ //往外扩张一个点，如果包含，就把这个点移除
        for(Point p : points) {
            cellRegion.recordY(p.y);
            cellRegion.recordX(p.x);
            surface.add(p);
        }
    }
    public void addInner(Point inner){
        if(innerCount < 0)
            innerSurface.add(inner);
    }
    public void addPoints(Point surface_point, LinkedList<Point> points){ //往外扩张一个点，如果包含，就把这个点移除
        if(points == null || points.size() == 0){//如果点没有扩张 检查是不是来自inner 是的话就不管了 不是的话加入tmp
            if(innerCount < 0)
                innerSurface.add(surface_point);
            return;
        }
        for(Point p : points) {
            cellRegion.recordY(p.y);
            cellRegion.recordX(p.x);
            surface.add(p);
        }
    }
}
