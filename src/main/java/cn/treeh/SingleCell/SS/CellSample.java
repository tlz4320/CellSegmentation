package cn.treeh.SingleCell.SS;


import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.O;
import cn.treeh.Utils.MMath;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
This is used to select (sample) counts of cell from front image,
and expand with edited watershed algorithm to define a better threshold.
 */
public class CellSample {

    ConcurrentLinkedQueue<Double> newThreshold = new ConcurrentLinkedQueue<>();
    double preThreshold = 1;
    int preCellCount = 1;
    AtomicInteger cellCount = new AtomicInteger(0);
    public class Processor implements Runnable{
        Mat front, record, original;
        Region workRegion;
        LinkedList<Region> cells;
        Processor(Mat _original, Mat _front, Region _workRegion){
            original = _original;
            front = _front;
            workRegion = _workRegion;
            cells = new LinkedList<>();
        }
        public void expand(int x, int y){
            LinkedList<int[]> points = new LinkedList<>();
            points.push(new int[]{x,y});
            Region region = new Region(x, y);
            int[] point;
            while(!points.isEmpty()) {
                point = points.pop();
                x = point[0];
                y = point[1];
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (record.get(MMath.ceil_floor(0, workRegion.width(), x + i - workRegion.minX),
                                MMath.ceil_floor(0, workRegion.height(), y + j - workRegion.minY))[0] == 0 &&
                                front.get(MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i),
                                        MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j))[0] != 0) {
                            record.put(MMath.ceil_floor(0, workRegion.maxX, x + i - workRegion.minX),
                                    MMath.ceil_floor(0, workRegion.maxY, y + j - workRegion.minY), 1);
                            region.recordX(MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i));
                            region.recordY(MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j));
                            points.push(new int[]{
                                    MMath.ceil_floor(workRegion.minX, workRegion.maxX, x + i),
                                    MMath.ceil_floor(workRegion.minY, workRegion.maxY, y + j)
                            });
                        }
                    }
                }
            }
            if(region.width() < 5 && region.height() < 5)
                return;
            cells.add(region);
        }
        public void calNewThreshold(){
            DescriptiveStatistics stats = new DescriptiveStatistics();
            double acc = 0;
            for(Region r : cells){
                if(r.width() == 0 || r.height() == 0)
                    continue;
                acc = 0;
                for(int x = r.minX; x < r.maxX; x++) {
                    for (int y = r.minY; y < r.maxY; y++) {
                        acc += original.get(x, y)[0];
                    }
                }
                acc /= r.square();
                if(acc != 0 && !Double.isNaN(acc))
                    stats.addValue(acc);
            }
            newThreshold.add(stats.getPercentile(50));
        }
        @Override
        public void run() {
            record = Mat.zeros(workRegion.width() + 1, workRegion.height() + 1, CvType.CV_8SC1);
            for(int x = workRegion.minX; x < workRegion.maxX; x++){
                for(int y = workRegion.minY; y < workRegion.maxY; y++){
                    if(record.get(x - workRegion.minX, y - workRegion.minY)[0] == 0){
                        expand(x, y);
                    }
                }
            }
            cellCount.addAndGet(cells.size());
            if(cells.size() != 0)
                calNewThreshold();
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
            }
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
    public Region findTissue(Mat original){
        Mat gaussian = new Mat();
        Imgproc.GaussianBlur(original, gaussian, new Size(1001, 1001), 0, 0);
        Mat binary = new Mat();
        Imgproc.threshold(gaussian, binary, 10, 255, Imgproc.THRESH_BINARY);
        Region res = new Region();
        for(int x = 0; x < binary.rows(); x++){
            for(int y = 0; y < binary.cols(); y++){
                if(binary.get(x,y)[0] != 0){
                    res.recordX(x);
                    res.recordY(y);
                }
            }
        }
        return res;
    }
    public double minThreshold(Mat gaussian, Region tissue){
        double acc = 0;
        for(int x = 0; x < gaussian.rows(); x++){
            for(int y = 0; y < gaussian.cols(); y++){
                if(tissue.contains(x, y))
                    continue;
                else
                    acc += gaussian.get(x, y)[0] / 255;
            }
        }
        return 255 * acc / (gaussian.cols() * gaussian.rows() - tissue.square());
    }
    public void main(SCconfig config) {
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
        double threshold = 50;
        Mat front = new Mat();
        Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
        ExecutorService executorService;
        double step = Math.sqrt(config.sample);
        Region tissue = findTissue(original);
        int stepLenX = (int) (Math.ceil(tissue.width() / step) + 1);
        int stepLenY = (int) (Math.ceil(tissue.height() / step) + 1);
        int index = 0;
        double min_threshold = minThreshold(gaussian, tissue);
        while(true) {
            preThreshold = threshold;
            executorService = Executors.newFixedThreadPool(config.thread_num);
            for (int x = tissue.minX; x < tissue.maxX; x += stepLenX) {
                for (int y = tissue.minY; y < tissue.maxY; y += stepLenY) {
                    executorService.submit(new Processor(gaussian, front, new Region(x, Math.min(x + stepLenX, tissue.maxX)
                            , y, Math.min(y + stepLenX, tissue.maxY))));
                }
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(Math.abs((cellCount.get() - preCellCount)) / (double)preCellCount < 0.05)
                break;
            threshold = 0;
            for(double t : newThreshold){
                threshold += t;
            }
            threshold = threshold / newThreshold.size();
            if(Math.abs((threshold - preThreshold)) / preThreshold < 0.05)
                break;
            preCellCount = cellCount.get();
            cellCount.set(0);
            newThreshold.clear();
            Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
            O.ptln("iterate: " + (index++) + "  finished");
        }
        O.ptln("final threshold: " + threshold);
    }
}
