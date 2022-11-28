package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.Iterator.InputCreator;
import cn.treeh.ToNX.Iterator.InputIterator;
import cn.treeh.ToNX.O;
import cn.treeh.ToNX.util.CommandLogUtil;
import cn.treeh.Utils.MMath;
import cn.treeh.Utils.Point;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.treeh.SingleCell.SS.ssNuclearSep.*;
import static cn.treeh.SingleCell.SSUtil.CommonMethod.splitCell;

public class ssRNASep {
    public double initThreshold(Mat m) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        int x, y;
        double tmp;
        for (x = 0; x < m.rows(); x++) {
            for (y = 0; y < m.cols(); y++) {
                tmp = m.get(x, y)[0];
                if (tmp != 0)
                    stats.addValue(tmp);
            }
        }
        return stats.getPercentile(90);
    }
    public void findSurface(Processor p){
        double tmp;
        int i, j;
        for(Cell c : p.cells){
            LinkedList<Point> sur = new LinkedList<>();
            for(int x = c.cellRegion.minX; x < c.cellRegion.maxX; x++) {
                for (int y = c.cellRegion.minY; y < c.cellRegion.maxY; y++) {
                    tmp = p.record.get(x - p.workRegion.minX, y - p.workRegion.minY)[0];
                    if(tmp < 2)
                        continue;
                    for(i = -1; i <= 1; i++){
                        for(j = -1; j <= 1; j++){
                            if(p.record.get(MMath.ceil_floor(0, p.workRegion.width(), x + i),
                                    MMath.ceil_floor(0, p.workRegion.height(), y + j))[0] != tmp) {
                                sur.add(new Point(x, y));
                            }
                        }
                    }
                }
            }
            c.innerSurface = new LinkedList<>();
            c.surface = sur;
            c.innerCount = 0;
            c.surfaceCount = sur.size();
        }
    }
    public void main(LinkedList<Processor> total_region, SCconfig config) {
        try {
            Mat original = Imgcodecs.imread(config.input_file);
            Mat gaussian = new Mat();
            Imgproc.GaussianBlur(original, gaussian, new Size(51, 51), 0, 0);
            ExecutorService executorService;
            double threshold = 0, preThreshold = 10000;
            AtomicInteger cellCount = total_region.get(0).cellCount;
            int preCellCount = total_region.get(0).cellCount.get();
            Mat front = gaussian;
            config._expand = config.expand;
            ConcurrentLinkedQueue<Double> newThreshold = total_region.getFirst().newThreshold;
            O.ptln("\nRNA_region size: " + newThreshold.size());
            for (Processor p : total_region) {
                p.reactivePoints();
                p.calRNAThreshold(front);
                p.findCell = false;
            }

            for (double t : newThreshold) {
                threshold += t;
            }

            if(newThreshold.size() == 0)
                O.ptln("???");
            else
                threshold = threshold / newThreshold.size();
            O.ptln("\nRNA segment threshold: " + threshold);
            threshold = 0;
            Mat cellRegion = Mat.zeros(front.rows() + 50, front.cols() + 50, CvType.CV_32SC1);
            if(config.input_file3 != null && config.input_file3.length() > 1) {
                InputIterator inputIterator = InputCreator.openInputIterator(config.input_file3);
                inputIterator.next();
                String[] tmp, pos;
                int posX, posY, type;
//            上41875  第二列加上18325
                while (inputIterator.hasNext()) {
                    tmp = inputIterator.next();
                    pos = tmp[0].split("[-]");
                    posX = Integer.parseInt(pos[2]);
                    posY = Integer.parseInt(pos[1]);
                    type = tmp[3].equals("HCC") ? 1 : 0;
                    for (int x = posX; x < posX + 50; x++) {
                        for (int y = posY; y < posY + 50; y++) {
                            cellRegion.put(x, y, type);
                        }
                    }
                }
            }
            Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
            for (Processor p : total_region) {
                p.reNewProcessor(front, original);
                p.setCellRegion(cellRegion);
                findSurface(p);
            }
            int index = 1;
            while (true) {
                if (Math.abs((threshold - preThreshold)) / preThreshold < 0.001)
                    break;
                preThreshold = threshold;
                executorService = Executors.newFixedThreadPool(config.thread_num);
                for (Processor p : total_region) {
                    p.reNewProcessor(front, original);
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
                Imgproc.threshold(gaussian, front, threshold, 255, Imgproc.THRESH_BINARY);
                O.ptln("iterate: " + (index++) + "  finished");
            }
            CommandLogUtil.log("RNA sep finished");
            splitCell(total_region);
            Mat mat = genResultImg(original, total_region, true);
            Imgcodecs.imwrite(config.output_file + ".finial.png", mat);
            writeFile(original, total_region, new FileWriter(config.output_file + ".final.txt"));
            Map<Double, Integer> cellSize = countSize(original, total_region);
            FileWriter writer = new FileWriter(config.output_file + ".rna_size_tmp.txt");
            for (Map.Entry<Double, Integer> e : cellSize.entrySet()) {
                writer.write(e.getKey().intValue() + "\t" + e.getValue() + "\n");
            }
            writer.flush();
            writer.close();
            cellSize = countReads(original, total_region);
            writer = new FileWriter(config.output_file + ".rna_reads_tmp.txt");
            for (Map.Entry<Double, Integer> e : cellSize.entrySet()) {
                writer.write(e.getKey().intValue() + "\t" + e.getValue() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
