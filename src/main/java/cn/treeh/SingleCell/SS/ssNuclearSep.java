package cn.treeh.SingleCell.SS;


import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.O;
import cn.treeh.ToNX.util.CommandLogUtil;
import cn.treeh.Utils.Region;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: each cell have their own threshold
//Second version of Nuclear Sep
public class ssNuclearSep {
    Mat front;
    ConcurrentLinkedQueue<Double> newThreshold = new ConcurrentLinkedQueue<>();
    double preThreshold = 99;
    AtomicInteger cellCount = new AtomicInteger(1);
    //先对这个区域高斯模糊，再取灰度值形成一个新的矩阵  //去背景

    public Region findTissue(Mat original) { //最开始的矩阵
        Mat gaussian = new Mat();  //创建一个Mat对象
        Imgproc.GaussianBlur(original, gaussian, new Size(1001, 1001), 0, 0);//模糊化处理
        Mat binary = new Mat();
        Imgproc.threshold(gaussian, binary, 10, 255, Imgproc.THRESH_BINARY);//小的值变成0，大的值变成255
        Region res = new Region(); //定义一个新的region对象
        res.minX = 99999;
        res.maxX = 0;
        res.minY = 99999;
        res.maxY = 0;
        for (int x = 0; x < binary.rows(); x++) {
            for (int y = 0; y < binary.cols(); y++) {
                if (binary.get(x, y)[0] != 0) {
//                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    res.recordX(x);
                    res.recordY(y);//循环到最后minX = minX和binary.rows()之间的最小值
                }
            }
        }
        O.ptln(res.minX + "  " + res.maxX + "   " + res.minY + "   " + res.maxY);
        return res;
    }
    public static Map<Double, Double> countBright(Mat original, LinkedList<Processor> total_region){
        TreeMap<Double, Double> res = new TreeMap<>();
        for (Processor p : total_region) {
            for (int x = 0; x < p.record.rows(); x++) {
                for (int y = 0; y < p.record.cols(); y++) {
                    res.put(p.record.get(x, y)[0], res.getOrDefault(p.record.get(x, y)[0], 0.0) +
                            original.get(p.workRegion.minX + x, p.workRegion.minY + y)[0]);
                }
            }
        }
        return res;
    }
    public static Map<Double, Integer> countSize(Mat original, LinkedList<Processor> total_region){
        TreeMap<Double, Integer> res = new TreeMap<>();
        for (Processor p : total_region) {
            for (int x = 0; x < p.record.rows(); x++) {
                for (int y = 0; y < p.record.cols(); y++) {
                    res.put(p.record.get(x, y)[0], res.getOrDefault(p.record.get(x, y)[0], 0) + 1);
                }
            }
        }
        return res;
    }
    public static Map<Double, Integer> countReads(Mat original, LinkedList<Processor> total_region){
        TreeMap<Double, Integer> res = new TreeMap<>();
        for (Processor p : total_region) {
            for (int x = 0; x < p.record.rows(); x++) {
                for (int y = 0; y < p.record.cols(); y++) {
                    if(original.get((x + p.workRegion.minX), (y + p.workRegion.minY))[0] != 0)
                        res.put(p.record.get(x, y)[0], res.getOrDefault(p.record.get(x, y)[0], 0) + 1);
                }
            }
        }
        return res;
    }
    public static void writeFile(Mat original, LinkedList<Processor> total_region, FileWriter writer) throws Exception{
        for (Processor p : total_region) {
            for (int x = 0; x < p.record.rows(); x++) {
                for (int y = 0; y < p.record.cols(); y++) {
                    if (p.record.get(x, y)[0] > 1 &&
                            original.get((x + p.workRegion.minX), (y + p.workRegion.minY))[0] != 0) {
                        writer.write((x + p.workRegion.minX) + "\t" +
                                (y + p.workRegion.minY) + "\t" +
                                ((int)(p.record.get(x, y)[0])) + "\n");
                    }
                }
            }
            CommandLogUtil.log("one region write finished");
        }
        writer.flush();
        writer.close();
    }
    public static Mat genResultImg(Mat original, LinkedList<Processor> total_region, boolean addEdge){
        Mat res = genResultImg(original, total_region);
        double tmp;
        int i, j;
        if(addEdge) {
            for (Processor p : total_region) {
                for(int x = 1; x < p.record.rows() - 1; x++) {
                    for (int y = 1; y < p.record.cols() - 1; y++) {
                        tmp = p.record.get(x, y)[0];
                        if(tmp < 2)
                            continue;
                        color: for(i = -1; i <= 1; i++){
                            for(j = -1; j <= 1; j++){
                                if(p.record.get(x + i, y + j)[0] != tmp) {
                                    res.put(x + p.workRegion.minX, y + p.workRegion.minY,
                                            0, 0, 255);
                                    break color;
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
    public static Mat genResultImg(Mat original, LinkedList<Processor> total_region){
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
            CommandLogUtil.log("one region write finished");
        }
        return result;
    }
    public double initThreshold(Mat m){
        LinkedList<Double> stats = new LinkedList<>();
        int x, y;
        double tmp;
        for(x = 0; x < m.rows(); x++){
            for(y = 0; y < m.cols(); y++){
                tmp = m.get(x, y)[0];
                if(tmp != 0)
                    stats.add(tmp);
            }
        }
        stats.sort(new Comparator<Double>() {
            @Override
            public int compare(Double aDouble, Double t1) {
                return t1.compareTo(aDouble);
            }
        });
        return stats.get(stats.size() / 10);
    }
    public LinkedList<Processor> initSSDNA(SCconfig config, Region tissue, Mat original){
        Mat original_ssDNA = Imgcodecs.imread(config.input_file2);
        Mat ssDNA = new Mat();
        Imgproc.GaussianBlur(original_ssDNA, ssDNA, new Size(11, 11), 0, 0);
        double threshold = initThreshold(ssDNA);
        double minThreshold = 0.3 * threshold;
        O.ptln("init ssDNA threshold: " + threshold);
        front = new Mat();
        Imgproc.threshold(ssDNA, front, threshold, 255, Imgproc.THRESH_BINARY);
        ExecutorService executorService;
        double step = Math.sqrt(config.sample);
        int stepLenX = (int) (Math.ceil(tissue.width() / step) + 1);
        int stepLenY = (int) (Math.ceil(tissue.height() / step) + 1);
        int index = 0;
//        double min_threshold = minThreshold(ssDNA, tissue);
        LinkedList<Processor> total_region = new LinkedList<>();
        for (int x = tissue.minX; x < tissue.maxX; x += stepLenX) {
            for (int y = tissue.minY; y < tissue.maxY; y += stepLenY) {
                total_region.add(new Processor(ssDNA, front, new Region(x,
                        Math.min(x + stepLenX, tissue.maxX - 1),
                        y, Math.min(y + stepLenY, tissue.maxY - 1)), cellCount, newThreshold, config));
            }
        }
        int preCellCount = 0;
        int steps = 0;
        while (true) {
            if(threshold < minThreshold)
                break;
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
            if((cellCount.get() == preCellCount)) {
                O.ptln("did not find any new nucleus");
                break;
            }
            preCellCount = cellCount.get();
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
            if(Math.abs(threshold - preThreshold) / preThreshold > config.alpha){
                threshold = preThreshold - config.alpha * preThreshold;
            }
            CommandLogUtil.log("new Threshold:" + threshold);
            newThreshold.clear();
            Imgproc.threshold(ssDNA, front, threshold, 255, Imgproc.THRESH_BINARY);
            O.ptln("iterate: " + (index++) + "  finished");
            steps++;
//            if(steps == 1)
//                break;
        }
        O.ptln("ssDNA sep finished begin to do RNA sep");
        Mat mat = genResultImg(ssDNA, total_region, true);
        Imgcodecs.imwrite(config.output_file + ".tmp.png", mat);
        Map<Double, Integer> cellSize = countSize(ssDNA, total_region);
        Map<Double, Double> cellSize2 = countBright(ssDNA, total_region);
        try {
            FileWriter writer = new FileWriter(config.output_file + ".ker_size_tmp.txt");
            for (Map.Entry<Double, Integer> e : cellSize.entrySet()) {
                writer.write(e.getKey().intValue() + "\t" + e.getValue() + "\n");
            }
            writer.flush();
            writer.close();
            writer = new FileWriter(config.output_file + ".ker_bright_tmp.txt");
            for (Map.Entry<Double, Double> e : cellSize2.entrySet()) {
                writer.write(e.getKey().intValue() + "\t" + e.getValue() + "\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception E){
            E.printStackTrace();
        }
//        throw new RuntimeException("Stop");
        return total_region;
    }


    public LinkedList<Processor> main(SCconfig config) {
        try {

        /*
        这个地方主要是用来计算threshold的，然后我的想法是这样的，首先当一张片子经过51*51的核进行高斯滤波之后，
        亮斑区域应该是我们想要的细胞，首先就是直接累加所有点的亮度值，找一个初期的阈值使得80%的亮度值都保留，
        在这个阈值之下做出一个比较粗略的front，在这个front的基础上划分成默认是10份，这10份进行一个类似watershed的算法，
        找出细胞和背景，取出这部分认为是细胞的区域，取中位数来当做阈值进行细胞选择，再用新的阈值进行重复上述步骤 直到找出来的细胞数没有明显变化为止
        之所以一开始用51*51的核进行高斯
         */
            Mat original = Imgcodecs.imread(config.input_file2);
            Mat gaussian = new Mat();
            Imgproc.GaussianBlur(original, gaussian, new Size(51, 51), 0, 0);
            Region tissue = findTissue(original);
            LinkedList<Processor> total_region = initSSDNA(config, tissue, null);
            return total_region;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
