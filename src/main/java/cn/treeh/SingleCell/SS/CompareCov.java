package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.O;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class CompareCov {
    public void main(SCconfig config) {
        try {
            System.loadLibrary("opencv_java452");
            Mat target = Imgcodecs.imread(config.input_file);
            Mat query = Imgcodecs.imread(config.input_file2);
            int nrow = target.rows(), ncol = target.cols();
            int qnrow = query.rows(), qncol = query.cols();
            int acc = 0;
            int total = 0;
            int r, g = 0, b = 0;
            Mat res = Mat.zeros(target.rows(), target.cols(), CvType.CV_8SC3);
            for(int x = 0; x < nrow; x++){
                for(int y = 0; y < ncol; y++){
                    r = b = 0;
                    if(target.get(x, y)[0] != 0){
                        total++;
                        r = 255;
                        if(x < qnrow && y < qncol && query.get(x, y)[0] != 0) {
                            b = 255;
                            acc++;
                        }
                        res.put(x, y, b, g, r);
                    }
                }
            }
            O.ptln(acc + "\t" + total);
            Imgcodecs.imwrite(config.output_file, res);

//            InputIterator inputIterator = InputCreator.openInputIterator(config.input_file);
//            String[] tmp;
//            inputIterator.next();
//            byte[][] totalPos = new byte[200000][200000];
//            Mat totalRes = Mat.zeros(200000, 200000, CvType.CV_8SC3);
//            int total_size = 0, x, y;
//            Region region = new Region();
//            while (inputIterator.hasNext()) {
//                tmp = inputIterator.next();
//                if(tmp[2].equals("0"))
//                    continue;
//                x = Integer.parseInt(tmp[0]);
//                y = Integer.parseInt(tmp[1]);
//                region.recordX(x);
//                region.recordY(y);
//                totalRes.put(x,y, 0, 255, 0);
//                total_size += totalPos[x][y] == 0 ? 1 : 0;
//                totalPos[x][y] = 1;
//            }
//            O.ptln("one file finished");
//
//            inputIterator.close();
//            inputIterator = InputCreator.openInputIterator(config.input_file2);
//            int contains = 0;
//            while (inputIterator.hasNext()) {
//                tmp = inputIterator.next();
//                x = Integer.parseInt(tmp[0]);
//                y = Integer.parseInt(tmp[1]);
//                region.recordX(x);
//                region.recordY(y);
//                totalRes.put(x, y, 255, totalRes.get(x, y)[1], 0);
//                contains += totalPos[x][y];
//                if(totalPos[x][y] != 0)
//                    totalPos[x][y] = 0;
//            }
//            O.ptln(contains +"\t"+ total_size);
//            totalRes.submat(region.minX, region.maxX, region.minY, region.maxY);
//            Imgcodecs.imwrite("T33_merge_res.png",totalRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
