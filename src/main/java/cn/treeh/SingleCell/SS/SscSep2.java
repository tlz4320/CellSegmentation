package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.O;
import cn.treeh.Utils.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.FileWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SscSep2 {
    AtomicInteger multiProcessor = new AtomicInteger(0);
    double threshold = 0.3;
    double bg_threshold = 0.08;
    double front_threshold = 3;
    double acc = 0;
    int level = 15;
    int edgeSize = 50;
    public class Processor implements Runnable{
        int[] region;
        Queue<int[]> unSplitRegion;
        Mat image;
        Mat font;
        Queue<int[]> splitLines;
        Processor(int[] r, Mat i, Mat f, Queue<int[]> usr, Queue<int[]> sl){
            region = r;
            image = i;
            unSplitRegion = usr;
            font = f;
            splitLines = sl;
        }
        @Override
        public void run() {
            split(image, font, region[0], region[1],region[2],region[3], level, unSplitRegion, threshold, edgeSize * edgeSize, splitLines);
        }
    }
    public int findMinLine(Mat image, int lx, int rx, int ty, int by, boolean row_col, int level, double threshold){
        int x, y, t;
        int res = -1;
        double min = Double.MAX_VALUE;
        int med;
        if(row_col) {
            med = (lx + rx) / 2;
            int leftx = med, rightx = med;
            while(leftx >= (lx + level) && rightx <= (rx - level)){
                leftx -= level / 2;
                rightx += level / 2;
                acc = 0;
                for(t = leftx; t < leftx + level; t++) {
                    for (y = ty; y < by; y++) {
                        acc += (image.get(t, y)[0]);
                    }
                }
                if(acc < min){
                    min = acc; res = leftx;
                    if(acc / (level * (by - ty)) <= threshold)
                        return res;
                }
                acc = 0;
                for(t = rightx; t > rightx - level; t--) {
                    for (y = ty; y < by; y++) {
                        acc += (image.get(t, y)[0]);
                    }
                }
                if(acc < min){
                    min = acc; res = rightx;
                    if(acc / (level * (by - ty))  <= threshold)
                        return res;
                }
            }
        }
        else{
            med = (ty + by) / 2;
            int topy = med, bottomy = med;
            while(topy >= (ty + level) && bottomy <= (by - level)){
                topy -= level / 2;
                bottomy += level / 2;
                acc = 0;
                for(t = topy; t < topy + level; t++) {
                    for (x = lx; x < rx; x++) {
                        acc += (image.get(t, x)[0]);
                    }
                }
                if(acc < min){
                    min = acc; res = topy;
                    if(acc / (level *(rx - lx)) <= threshold)
                        return res;
                }
                acc = 0;
                for(t = bottomy; t > bottomy - level; t--) {
                    for (x = lx; x < rx; x++) {
                        acc += (image.get(t, x)[0]);
                    }
                }
                if(acc < min){
                    min = acc; res = bottomy;
                    if(acc/ (level *(rx - lx)) <= threshold)
                        return res;
                }
            }
        }
        acc = min;
        return res;
    }
    public boolean checkEntropy(Mat image, int lx, int rx, int ty, int by, double threshold, int sqthreshold){
        int x, y;
        acc = 0;
        for(x = lx; x <= rx; x++){
            for(y = ty; y <= by; y++){
                acc += image.get(x, y)[0];
            }
        }
        if((rx - lx) > 1.5 * Math.sqrt(sqthreshold) || (by - ty) > 1.5 * Math.sqrt(sqthreshold))
            return false;
        return ((rx - lx) < Math.sqrt(sqthreshold) &&
                (by - ty) < Math.sqrt(sqthreshold)) ||
                acc / ((rx - lx) * (by - ty)) > 4 * threshold ||
                ((rx - lx) * (by - ty)) < sqthreshold;

    }
    public int[] expand(Mat image, int lx, int rx, int ty, int by, boolean row_col, int minLine) {
        double acc = 0, lineAcc1 = 0, lineAcc2 = 0;
        int lineBig = minLine + 1, lineSmall = minLine - 1, res1 = minLine, res2 = minLine;
        int x, y;
        if (row_col) {
            for (y = ty; y <= by; y++) {
                acc += image.get(minLine, y)[0];
                if (lineBig == image.rows())
                    lineAcc1 = Double.MAX_VALUE;
                else
                    lineAcc1 += image.get(lineBig, y)[0];
                if (lineSmall == -1)
                    lineAcc2 = Double.MAX_VALUE;
                else
                    lineAcc2 += image.get(lineSmall, y)[0];
            }
            do {
                if (lineAcc1 < lineAcc2) {
                    acc += lineAcc1;
                    res1 = lineBig;
                    lineBig += 1;
                    if (lineBig >= image.rows())
                        break;
                    for (y = ty; y <= by; y++) {
                        lineAcc1 += image.get(lineBig, y)[0];
                    }
                } else {
                    acc += lineAcc2;
                    res2 = lineSmall;
                    lineSmall -= 1;
                    if (lineSmall < 0)
                        break;
                    for (y = ty; y <= by; y++) {
                        lineAcc2 += image.get(lineSmall, y)[0];
                    }
                }
            } while (acc / ((lineBig - lineSmall - 1) * (by - ty)) < bg_threshold);

        } else {
            for (x = lx; x <= rx; x++) {
                acc += image.get(x, minLine)[0];
                if (lineBig == image.cols())
                    lineAcc1 = Double.MAX_VALUE;
                else
                    lineAcc1 += image.get(x, lineBig)[0];
                if (lineSmall == -1)
                    lineAcc2 = Double.MAX_VALUE;
                else
                    lineAcc2 += image.get(x, lineSmall)[0];
            }
            do {
                if (lineAcc1 < lineAcc2) {
                    acc += lineAcc1;
                    res1 = lineBig;
                    lineBig += 1;
                    if (lineBig >= image.cols())
                        break;
                    for (x = lx; x <= rx; x++) {
                        lineAcc1 += image.get(x, lineBig)[0];
                    }
                } else {
                    acc += lineAcc2;
                    res2 = lineSmall;
                    lineSmall -= 1;
                    if (lineSmall < 0)
                        break;
                    for (x = lx; x <= rx; x++) {
                        lineAcc2 += image.get(x, lineSmall)[0];
                    }
                }
            } while (acc / ((lineBig - lineSmall - 1) * (rx - lx)) < bg_threshold);
        }
        return new int[]{res2, res1};


    }
    public boolean checkFront(Mat front, int lx, int rx, int ty, int by){
        int x, y;
        acc = 0;
        for(x = lx; x <= rx; x++){
            for(y = ty; y <= by; y++){
                acc += front.get(x, y)[0];
            }
        }
        return acc / (rx - lx) / (by - ty) > front_threshold;
    }
    public void split(Mat image,Mat font, int lx, int rx, int ty, int by, int level, Queue<int[]> unSplitRegion, double threshold, int sqthreshold, Queue<int[]> splitLines) {
        if (rx <= lx || by <= ty) {
            multiProcessor.addAndGet(-1);
            return;
        }
        if (checkEntropy(image, lx, rx, ty, by, threshold, sqthreshold)) {
            multiProcessor.addAndGet(-1);
            if(checkFront(font, lx, rx, ty, by))
                splitLines.add(new int[]{lx, rx, ty, by});
            return;
        }
        //总是从大一点的边先去找最小值 如果说找不到再去另一边去找一找
        //另外 对边长也有要求 不能太短了 太短了不能分
        int selectRow = -1, selectCol = -1;
        double rowAcc = Double.MAX_VALUE, colAcc = Double.MAX_VALUE;
        if (rx - lx > by - ty) {
            selectRow = findMinLine(font, lx, rx, ty, by, true, level, front_threshold);
            rowAcc = acc;
            if ((by - ty) > 1.2 * Math.sqrt(sqthreshold)) {
                selectCol = findMinLine(font, lx, rx, ty, by, false, level, front_threshold);
                colAcc = acc;
            }
            if (rowAcc < colAcc)
                selectCol = -1;
            else
                selectRow = -1;
        } else {
            selectCol = findMinLine(font, lx, rx, ty, by, false, level, front_threshold);
            colAcc = acc;

            if ((rx - lx) > 1.2 * Math.sqrt(sqthreshold)) {
                selectRow = findMinLine(font, lx, rx, ty, by, true, level, front_threshold);
                rowAcc = acc;
            }
            if (rowAcc < colAcc)
                selectCol = -1;
            else
                selectRow = -1;
        }
        if (selectCol == -1 && selectRow == -1) {
            multiProcessor.addAndGet(-1);
            return;
        }
        if (selectRow != -1) {
            int[] exp = expand(image, lx, rx, ty, by, true, selectRow);
            unSplitRegion.add(new int[]{lx, exp[0] - 1, ty, by});
            unSplitRegion.add(new int[]{exp[1] + 1, rx, ty, by});
        } else {
            int[] exp = expand(image, lx, rx, ty, by, false, selectCol);
            unSplitRegion.add(new int[]{lx, rx, ty, exp[0] - 1});
            unSplitRegion.add(new int[]{lx, rx, exp[1] + 1, by});
        }
        multiProcessor.addAndGet(-1);
    }

    public void main(SCconfig config) {
        System.loadLibrary("opencv_java452");
        try {
            Mat image = Imgcodecs.imread("D:\\T33_segment\\T33_Reg1_RNA.png");
            Mat font = Imgcodecs.imread("D:\\T33_segment\\T33_Reg1_RNA_gus_filter.png");
            ConcurrentLinkedQueue<int[]> unSplitRegion = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<int[]> splitLines = new ConcurrentLinkedQueue<>();
            unSplitRegion.add(new int[]{0,image.cols() - 1, 0 ,image.cols() - 1});
            int[] region;
            ExecutorService executorService = Executors.newFixedThreadPool(config.thread_num);
            while(multiProcessor.get() != 0 || unSplitRegion.size() != 0){
                if(unSplitRegion.size() == 0){
                    while(multiProcessor.get() != 0 && unSplitRegion.size() == 0){
                        Thread.sleep(1000);
                    }
                    if(unSplitRegion.size() == 0 && multiProcessor.get() == 0)
                        break;
                }
                region = unSplitRegion.poll();
                executorService.execute(new Processor(region, image, font, unSplitRegion, splitLines));
                multiProcessor.addAndGet(1);

            }
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
            Mat original = Imgcodecs.imread("D:\\T33_segment\\T33_Reg1_RNA.png", Imgcodecs.IMREAD_GRAYSCALE);
            O.ptln("Finished Sep");
            O.ptln("Begin to add rectangle to original plot");
            OpenCV.paintLines(original, splitLines, 3);
            int x, y, label = 0;
            int minx = 0, miny = 0;
            FileWriter writer = new FileWriter(config.output_file);
            for(int[] pos : splitLines){
                label++;
                for(x = pos[0]; x <= pos[1]; x++){
                    for(y = pos[2]; y <= pos[3]; y++){
                        if(original.get(x, y)[0] != 0){
                            writer.write((minx + x) + "\t" + (miny + y) + "\t" + (int)original.get(x,y)[0] + "\t" + label + "\n");
                        }
                    }
                }
            }
            writer.flush();
            writer.close();
            Imgcodecs.imwrite("D:\\T33_segment\\T33_Reg1_RNA_res.png", original);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
