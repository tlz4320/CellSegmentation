package cn.treeh.SingleCell.SS;


import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.O;
import cn.treeh.Utils.MMath;
import cn.treeh.Utils.Point;
import cn.treeh.Utils.Region;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
This is used to select (sample) counts of cell from front image,
and expand with edited watershed algorithm to define a better threshold.
 */
public class CellSample2 {
    Mat front;
    ConcurrentLinkedQueue<Double> newThreshold = new ConcurrentLinkedQueue<>();
    double preThreshold = 99;
    int preCellCount = 1;
    AtomicInteger cellCount = new AtomicInteger(0);
    public class Processor implements Runnable{ //implements 实现接口
        Mat  record, original; //声明三个matrix变量 Mat类表示用于存储图像的矩阵对象
        Region workRegion;//初始化 几个像素去找？ //region类型 正方形 圆形填空 降低阈值
        LinkedList<Cell> cells; //调用cell里面的cells 由cell组成的list cells 存细胞质心
        Processor(Mat _original, Region _workRegion){ //参数 初始化
            original = _original; //赋值
            workRegion = _workRegion;
            cells = new LinkedList<>(); //建立一个cell list
            record = Mat.zeros(workRegion.width() + 1, workRegion.height() + 1, CvType.CV_32SC1);
        }
        public void findNewCell(int x, int y){ //做细胞扩张然后计算平均值，输入一个点的x，y值
            int cellID = cellCount.addAndGet(1);
            LinkedList<Point> points = new LinkedList<>();//创建point list (由x和y组成)
            LinkedList<Point> surface = new LinkedList<>();
            points.push(new Point(x, y)); //在ponits中增加新的point(x,y)
            Point point; //初始化？？
            int tmpX, tmpY, workX, workY;
            while(!points.isEmpty()) { //如果值是空 list中没有point
                point = points.pop(); //points中的第一个
                surface.add(point); //将非空点加入surface
                x = point.x;
                y = point.y;
                for (int i = -1; i <= 1; i++) {//初始化；布尔表达式；更新   //
                    for (int j = -1; j <= 1; j++) { //return Math.max(min, Math.min(max, v)) 保证v在最大值和最小值之间
                        tmpX = MMath.ceil_floor(0, workRegion.width(), x + i - workRegion.minX);
                        tmpY = MMath.ceil_floor(0, workRegion.height(), y + j - workRegion.minY);
                        workX = MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i);
                        workY = MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j);
                        if (record.get(tmpX, tmpY)[0] == 0 &&
                                front.get(workX, workY)[0] != 0) {
                            record.put(tmpX, tmpY, cellID);
                            points.push(new Point(workX,workY));
                        }
                    }
                }
            }

            cells.add(new Cell(surface, cellID));
        }
        public void calNewThreshold(){
            DescriptiveStatistics stats = new DescriptiveStatistics();
            double acc = 0;
            Region r;
            for(Cell c : cells){
                r = c.getCellRegion();//得到正方形区域
                if(r.width() == 0 || r.height() == 0) //高度和宽度都为0的情况下，判定为假的细胞
                    continue;
                acc = 0;
                for(int x = r.minX; x < r.maxX; x++) {
                    for (int y = r.minY; y < r.maxY; y++) {
                        acc += original.get(x, y)[0];  //original 给个MAt对象
                    }
                }//遍历x轴再遍历y轴，把该正方形区域的值都加起来
                acc /= r.square(); // acc = acc/r.square() r.square是 长X宽
                if(acc != 0 && !Double.isNaN(acc)) // 判断平均值是否是非数字以及非0
                    stats.addValue(acc);
            }
            if(!Double.isNaN(stats.getPercentile(50)))
                newThreshold.add(stats.getPercentile(50));
        }
        public boolean expandSurface(int x, int y, LinkedList<Point> points, int cellID) {
            boolean inner = true;
            int tmpX, tmpY, workX, workY;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    tmpX = MMath.ceil_floor(0, workRegion.width(), x + i - workRegion.minX);
                    tmpY = MMath.ceil_floor(0, workRegion.height(), y + j - workRegion.minY);
                    workX = MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i);
                    workY = MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j);
                    if (record.get(tmpX, tmpY)[0] == 0 &&
                            front.get(workX, workY)[0] != 0) {
                        inner = false;
                        record.put(tmpX, tmpY, cellID);
                        points.push(new Point(workX, workY));
                    }
                }
            }
            return inner;
        }
        public void expandCells(){
            boolean isChanged = true; //
            Point p;
            while(isChanged){
                isChanged = false; //所有细胞一起阔 //java的队列 linkedlist
                for(Cell c : cells) {
                    //cell里加布尔值 surface没变化就不扩张
                    while ((p = c.getPoint()) != null) {
                        LinkedList<Point> newPoints = new LinkedList<>();
                        isChanged = !expandSurface(p.x, p.y, newPoints, c.cellID);
                        c.addPoints(p, newPoints);
                    }
                    c.resetCell();
                }

            }
        }
        @Override
        public void run() {
            expandCells();
//            O.ptln("one circle finished");
            for(int x = workRegion.minX; x < workRegion.maxX; x++){
                for(int y = workRegion.minY; y < workRegion.maxY; y++){
                    if(record.get(x - workRegion.minX, y - workRegion.minY)[0] == 0 &&
                        front.get(x, y)[0] != 0){
                        findNewCell(x, y);
                    }
                }
            }
            O.ptln("find new Cell finished");
            if(cells.size() != 0) {
                for(Cell c : cells){
                    c.resetThreshold();
                }
                calNewThreshold();
            }
        }
    }
    double initThreshold(Mat gaussian){
        ArrayList<Double> res = new ArrayList<>(1 << 30);
        double acc = 0, temp, threshold = 0;
        for(int x = 0; x < gaussian.rows(); x++){
            for(int y = 0; y < gaussian.cols(); y++){
                temp = gaussian.get(x,y)[0];
                if(temp != 0){
                    res.add(temp);
                    acc += temp / 255;
                }
            }
        }
        res.sort(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return o1.compareTo(o2);
            } //返回值是整型
        });
        temp = 0;
        acc = acc * 0.8;
        for(double d : res){
            threshold = d;
            temp += d / 255;
            if(temp > acc)
                break;
        }
        return threshold;
    }
    //先对这个区域高斯模糊，再取灰度值形成一个新的矩阵  //去背景
    public Region findTissue(Mat original){ //最开始的矩阵
        Mat gaussian = new Mat();  //创建一个Mat对象
        Imgproc.GaussianBlur(original, gaussian, new Size(1001, 1001), 0, 0);//模糊化处理
        Mat binary = new Mat();
        Imgproc.threshold(gaussian, binary, 10, 255, Imgproc.THRESH_BINARY);//小的值变成0，大的值变成255
        Region res = new Region(); //定义一个新的region对象
        res.minX = 99999;
        res.maxX = 0;
        res.minY = 99999;
        res.maxY = 0;
        for(int x = 0; x < binary.rows(); x++){
            for(int y = 0; y < binary.cols(); y++){
                if(binary.get(x,y)[0] != 0){
//                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    res.recordX(x);
                    res.recordY(y);//循环到最后minX = minX和binary.rows()之间的最小值
                }
            }
        }
        O.ptln(res.minX + "  " + res.maxX + "   " + res.minY + "   " + res.maxY);
        return res;
    }
    public double minThreshold(Mat gaussian, Region tissue){ //这里应该是计算初始阈值，返回
        double acc = 0;
        for(int x = 0; x < gaussian.rows(); x++){
            for(int y = 0; y < gaussian.cols(); y++){//遍历这个矩阵每个像素点
                if(tissue.contains(x, y)) //判断tissue region中是否包含了这个点，如果是就重新循环
                    continue;
                else
                    acc += gaussian.get(x, y)[0] / 255;// 如果tissue resgion中没有包含这个点，acc加灰度值
            }
        }
        return acc / (gaussian.cols() * gaussian.rows() - tissue.square()) * 255; //总的Mat的面积减去tissue的面积 acc值除以这个面积
    }
    public Mat paintTmp(List<Processor> ps, Mat original){
        Mat res = Mat.zeros(original.rows(), original.cols(), CvType.CV_8SC1);
        for(Processor p : ps){
            for(Cell c : p.cells){
                for(Point pt : c.innerSurface)
                    res.put(pt.x, pt.y, 255);
            }
        }
        return res;
    }
    public LinkedList<Point> cellInner(Mat Img, Cell cell){
        LinkedList<Point> res = new LinkedList<>();
        boolean inner = false;
        Mat tmpImg = Mat.zeros(cell.getCellRegion().width() + 1, cell.getCellRegion().height() + 1, CvType.CV_8SC1);
        for(Point p : cell.innerSurface){
            tmpImg.put(p.x - cell.getCellRegion().minX, p.y- cell.getCellRegion().minY, 1);
        }
        for(int x = cell.cellRegion.minX; x <= cell.cellRegion.maxX; x++){
            for(int y = cell.cellRegion.minY; y <= cell.cellRegion.maxY; y++){
                if(tmpImg.get(x - cell.getCellRegion().minX,y - cell.getCellRegion().minY)[0] != 0) {
                    inner = !inner;
                    if (Img.get(x, y)[0] != 0)
                        res.add(new Point(x, y));
                }
                else if (inner && Img.get(x, y)[0] != 0)
                    res.add(new Point(x, y));
            }
        }
        return res;
    }
    public Mat genResultImg(Mat original, LinkedList<Processor> total_region, boolean addEdge){
        Mat res = genResultImg(original, total_region);
        if(addEdge) {
            for (Processor p : total_region) {
                for (Cell c : p.cells) {
                    for (Point pt : c.innerSurface)
                        res.put(pt.x, pt.y, 0,0, 255);
                }
            }
        }
        return res;
    }
    public Mat genResultImg(Mat original, LinkedList<Processor> total_region){
        Mat result = Mat.zeros(original.rows(), original.cols(), CvType.CV_8SC3);
//            FileWriter writer = new FileWriter(config.output_file);
        for (Processor p : total_region) {
            for(int x = 0; x < p.record.rows(); x++) {
                for (int y = 0; y < p.record.cols(); y++) {
                    if(p.record.get(x, y)[0] != 0 &&
                            original.get((x + p.workRegion.minX), (y + p.workRegion.minY))[0] != 0) {
                        result.put(x + p.workRegion.minX, y + p.workRegion.minY,
                                255, 255 ,255);
                    }
                }
            }
            O.ptln("one region write finished");
        }
//        Imgcodecs.imwrite(path, result);
        return result;
    }
    public double InitThreshold(Mat m){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int x, y;
        double tmp;
        for(x = 0; x < m.rows(); x++){
            for(y = 0; y < m.cols(); y++){
                tmp = m.get(x, y)[0];
                if(tmp != 0)
                    stats.addValue(tmp);
            }
        }
        return stats.getPercentile(50);
    }
    public void main(SCconfig config) {
        try {
            System.loadLibrary("opencv_java452");
        /*
        这个地方主要是用来计算threshold的，然后我的想法是这样的，首先当一张片子经过51*51的核进行高斯滤波之后，
        亮斑区域应该是我们想要的细胞，首先就是直接累加所有点的亮度值，找一个初期的阈值使得80%的亮度值都保留，
        在这个阈值之下做出一个比较粗略的front，在这个front的基础上划分成默认是10份，这10份进行一个类似watershed的算法，
        找出细胞和背景，取出这部分认为是细胞的区域，取中位数来当做阈值进行细胞选择，再用新的阈值进行重复上述步骤 直到找出来的细胞数没有明显变化为止
        之所以一开始用51*51的核进行高斯
         */
            Mat original = Imgcodecs.imread(config.input_file);
            Mat gaussian = new Mat();
            Imgproc.GaussianBlur(original, gaussian, new Size(51, 51), 0, 0);
//            gaussian = Imgcodecs.imread("tmp_gus.png");
            double threshold = 50;
            front = new Mat();
            Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
//        Imgcodecs.imwrite("D:\\program\\others\\SS\\for_test.png", front);
            ExecutorService executorService;
            double step = Math.sqrt(config.sample);
            Region tissue = findTissue(original);
            int stepLenX = (int) (Math.ceil(tissue.width() / step) + 1);
            int stepLenY = (int) (Math.ceil(tissue.height() / step) + 1);
            int index = 0;
            double min_threshold = minThreshold(gaussian, tissue);
            LinkedList<Processor> total_region = new LinkedList<>();
            for (int x = tissue.minX; x < tissue.maxX; x += stepLenX) {
                for (int y = tissue.minY; y < tissue.maxY; y += stepLenY) {
                    total_region.add(new Processor(gaussian, new Region(x,
                            Math.min(x + stepLenX, tissue.maxX - 1),
                            y, Math.min(y + stepLenY, tissue.maxY - 1))));
                }
            }
            while (true) {
                if (Math.abs((threshold - preThreshold)) / preThreshold < 0.001)
                    break;
                preThreshold = threshold;
                executorService = Executors.newFixedThreadPool(config.thread_num);
                for (Processor p : total_region) {
                    executorService.submit(p);
                }
                executorService.shutdown();
                try {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                O.ptln("CellCounts:" + cellCount.get());
//            if((cellCount.get() == preCellCount))
//                break;
                threshold = 0;
                for (double t : newThreshold) {
                    threshold += t;
                }
                if(newThreshold.size() == 0){
                    O.ptln("???");
                    break;
                }
                else
                    threshold = threshold / newThreshold.size();
                O.ptln("new Threshold:" + threshold);
                newThreshold.clear();
                Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
                O.ptln("iterate: " + (index++) + "  finished");
            }
            O.ptln("sep finished begin to write file");
//            FileWriter writer = new FileWriter(config.output_file);
//            for(Processor p : total_region){
//                for(int x = 0; x < p.record.rows(); x++){
//                    for(int y = 0; y < p.record.cols(); y++){
//                        writer.write((x + p.workRegion.minX) + "\t" + (y + p.workRegion.minY) + "\t" + (int)p.record.get(x, y)[0] + "\n");
//                    }
//                }
//            }
//            writer.close();
            Mat result = genResultImg(original, total_region,true);
            Imgcodecs.imwrite(config.output_file, result);
//            writer.close();
//        Mat tmpRes = paintTmp(total_region, original);
//        Mat rrr = Mat.zeros(tmpRes.rows(), tmpRes.cols(), CvType.CV_8SC3);
//        for(int ix = 0; ix < original.rows(); ix++){
//            for(int it = 0; it < original.cols(); it++){
//                rrr.put(ix, it, 0, original.get(ix, it)[0] / 1.5,tmpRes.get(ix, it)[0] == 0 ? 0 : 255);
//            }
//        }
//        Imgcodecs.imwrite("D:\\program\\others\\SS\\for_test2.png", rrr);
//        O.ptln("final threshold: " + threshold);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
