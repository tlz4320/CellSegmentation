package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.util.CommandLogUtil;
import cn.treeh.Utils.MMath;
import cn.treeh.Utils.Point;
import cn.treeh.Utils.Region;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Processor implements Runnable{ //implements 实现接口
    public boolean findCell = true;
    Mat record, original, front; //声明三个matrix变量 Mat类表示用于存储图像的矩阵对象

    public Mat getRecord() {
        return record;
    }

    public Mat getOriginal() {
        return original;
    }

    public Mat getFront() {
        return front;
    }

    public Region getWorkRegion() {
        return workRegion;
    }

    public LinkedList<Cell> getCells() {
        return cells;
    }

    public AtomicInteger getCellCount() {
        return cellCount;
    }

    public ConcurrentLinkedQueue<Double> getNewThreshold() {
        return newThreshold;
    }

    public Mat getCellThreshold() {
        return cellThreshold;
    }

    Region workRegion;//初始化 几个像素去找？ //region类型 正方形 圆形填空 降低阈值
    LinkedList<Cell> cells; //调用cell里面的cells 由cell组成的list cells 存细胞质心
    AtomicInteger cellCount;
    ConcurrentLinkedQueue<Double> newThreshold;
    SCconfig config;

    Processor(Mat _original, Mat _front, Region _workRegion, AtomicInteger _cellCount,
              ConcurrentLinkedQueue<Double> _newThreshold, SCconfig _config){ //参数 初始化
        original = _original; //赋值
        front = _front;
        workRegion = _workRegion;
        cells = new LinkedList<>(); //建立一个cell list
        record = Mat.zeros(workRegion.width() + 1, workRegion.height() + 1, CvType.CV_32SC1);
        cellCount = _cellCount;
        newThreshold = _newThreshold;
        config = _config;
    }
    public void reactivePoints(){
        for(int x = 0; x < record.rows(); x++){
            for(int y = 0; y < record.cols(); y++){
                if(record.get(x, y)[0] == 1){
                    record.put(x, y, 0);
                }
            }
        }
    }
    public void destroyPoints(Cell cell){
        destroyPoints(cell, false);
    }
    public void destroyPoints(Cell cell, boolean stopOnEdge){
        int tmpX, tmpY, workX, workY, x, y;
        boolean stop = false;
        boolean addPoint = false;
        cell.destroy = cell.innerSurface;
        LinkedList<Point> newDes = new LinkedList<>();
        while(true) {
            for (Point tmp : cell.destroy) {
                newDes = new LinkedList<>();
                x = tmp.x;
                y = tmp.y;
                addPoint = false;
                for (int oi = 0; oi <= config._expand; oi++) {
                    for (int oj = 0; oj <= config._expand; oj++) {
                        for (int i = -2; i <= 2; i += 1) {
                            for (int j = -2; j <= 2; j += 1) {
                                tmpX = MMath.ceil_floor(0, workRegion.width(), x + i - workRegion.minX);
                                tmpY = MMath.ceil_floor(0, workRegion.height(), y + j - workRegion.minY);
                                workX = MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i);
                                workY = MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j);
                                if (record.get(tmpX, tmpY)[0] == 0 &&
                                        front.get(workX, workY)[0] != 0 &&
                                        cell.threshold > config.beta * original.get(workX, workY)[0]) {
                                    record.put(tmpX, tmpY, 1);
                                    if (!stop) {
                                        newDes.push(new Point(workX, workY));
                                        addPoint = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!addPoint && stopOnEdge)
                    stop = true;
            }
            if(newDes.size() == 0 || stop)
                break;
            else {
                cell.destroy.clear();
                cell.destroy = newDes;
            }
        }
    }
    public void findNewCell(int x, int y){ //做细胞扩张然后计算平均值，输入一个点的x，y值
        int cellID = cellCount.addAndGet(1);
        LinkedList<Point> points = new LinkedList<>();//创建point list (由x和y组成)
        points.push(new Point(x, y)); //在ponits中增加新的point(x,y)
        record.put(x - workRegion.minX, y - workRegion.minY, cellID);
        Cell cell = new Cell(points, cellID);
        while(true){
            cell.resetThreshold(original);
            if(!expandOneLayer(cell, 0))
                break;
        }
        if(true) { //cell.goodCell()
            cells.add(cell);
            destroyPoints(cell);
        }
    }
    public void splitOldCell(int x, int y, int oldCellID, LinkedList<Cell> newcell){ //做细胞扩张然后计算平均值，输入一个点的x，y值
        int cellID = cellCount.addAndGet(1);
        LinkedList<Point> points = new LinkedList<>();//创建point list (由x和y组成)
        points.push(new Point(x, y)); //在ponits中增加新的point(x,y)
        record.put(x - workRegion.minX, y - workRegion.minY, cellID);
        Cell cell = new Cell(points, cellID);
        double celltype = 0;
        if(cellThreshold != null)
            celltype = cellThreshold.get(x, y)[0];
        else
            celltype = 0;
        while(cell.getCellRegion().square() < (celltype == 1 ? 2500 : 900)){
//            cell.resetThreshold(original);
            if(!expandOneLayer(cell, oldCellID))
                break;
        }
        newcell.add(cell);
    }
    public void calNewThreshold(){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(Cell c : cells){
            stats.addValue(c.threshold);
        }
        if(!Double.isNaN(stats.getPercentile(50)))
            newThreshold.add(stats.getPercentile(50));
        else
            CommandLogUtil.log("No cell found in this region");
    }
    public boolean expandSurface(int x, int y, LinkedList<Point> points, Cell c, int background) {
        boolean inner = true;
        int tmpX, tmpY, workX, workY;
        for (int i = -config._expand; i <= config._expand; i++) {
            for (int j = -config._expand; j <= config._expand; j++) {
                tmpX = MMath.ceil_floor(0, workRegion.width(), x + i - workRegion.minX);
                tmpY = MMath.ceil_floor(0, workRegion.height(), y + j - workRegion.minY);
                workX = MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i);
                workY = MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j);
                if (record.get(tmpX, tmpY)[0] == background &&
                        front.get(workX, workY)[0] != 0 &&
                        (!findCell || c.threshold <= config.beta * original.get(workX, workY)[0])) {
                    inner = false;
                    record.put(tmpX, tmpY, c.cellID);
                    points.push(new Point(workX, workY));
                }
            }
        }
        return inner;
    }
    public boolean expandOneLayer(Cell c, int background){
        Point p;
        LinkedList<Point> newPoints = new LinkedList<>();
        //cell里加布尔值 surface没变化就不扩张
        while ((p = c.getPoint()) != null) {
            if(expandSurface(p.x, p.y, newPoints, c, background)){
                c.addInner(p);
            }
        }
        c.addPoints(newPoints);
        c.resetCell();
        return newPoints.size() != 0;
    }
    Mat cellThreshold;
    public void setCellRegion(Mat r){
        cellThreshold = r;
    }
    public void expandCells(){
        boolean isChanged = true; //
        double celltype;
        while(isChanged){
            isChanged = false; //所有细胞一起阔 //java的队列 linkedlist
            for(Cell c : cells) {
                if(cellThreshold != null)
                    celltype = cellThreshold.get(c.kernel.x, c.kernel.y)[0];
                else
                    celltype = -1;
                if(findCell || celltype < 0 || c.getCellRegion().square() < (celltype == 1 ? 2500 : 900))
                    isChanged = isChanged || expandOneLayer(c, 0);
                if(findCell)
                    destroyPoints(c, false);
            }
        }
    }
    public void reNewProcessor(Mat _front, Mat _original){
           front = _front;
           original = _original;
    }
    public void calRNAThreshold(Mat _original){
        for(Cell c : cells){
            c.reCalculateThreshold(_original);
        }
        calNewThreshold();
    }
    public void splitCell(Cell c, LinkedList<Cell> newCell) {
        for (int x = c.getCellRegion().minX; x < c.getCellRegion().maxX; x++) {
            for (int y = c.getCellRegion().minY; y < c.getCellRegion().maxY; y++) {
                if (record.get(x - workRegion.minX, y - workRegion.minY)[0] == c.cellID) {
                    splitOldCell(x, y, c.cellID, newCell);
                }
            }
        }
    }
    @Override
    public void run() {
        for(int x = 0; x < record.rows(); x++){
            for(int y = 0; y < record.cols(); y++){
                if(record.get(x, y)[0] == 1)
                    record.put(x, y, 0);
            }
        }
        expandCells();
        if(findCell) {
            for (int x = workRegion.minX; x < workRegion.maxX; x++) {
                for (int y = workRegion.minY; y < workRegion.maxY; y++) {
                    if (record.get(x - workRegion.minX, y - workRegion.minY)[0] == 0 &&
                            front.get(x, y)[0] != 0) {
                        findNewCell(x, y);
                    }
                }
            }
            CommandLogUtil.log("Find new Cells finished");
        }
        if(cells.size() != 0) {
            for(Cell c : cells){
                c.resetThreshold(original);
            }
            calNewThreshold();
        }
    }
}