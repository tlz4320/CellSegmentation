package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.Iterator.InputCreator;
import cn.treeh.ToNX.Iterator.InputIterator;
import cn.treeh.ToNX.O;
import cn.treeh.Utils.Region;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.LinkedList;
import java.util.Locale;

public class GenerateIMG {

    public void main(SCconfig config) {
        System.loadLibrary("opencv_java452");
        try {

            InputIterator inputIterator = InputCreator.openInputIterator(config.input_file);
            String[] temp;
            Region region = new Region();
            int x, y, value, tmpx;
            LinkedList<int[][]> totalPos = new LinkedList<>();
            int step = 0;
            int[][] tmp = new int[3][100000];
            while (inputIterator.hasNext()) {
                temp = inputIterator.next();
                if(temp[0].startsWith("#"))
                    continue;
                if (temp[1].toLowerCase(Locale.ROOT).contains("x")) {
                    continue;
                }
                x = Integer.parseInt(temp[1]);
                y = Integer.parseInt(temp[2]);
                value = Integer.parseInt(temp[3]);
                if(config.ExchangeXY){
                    tmpx = x;
                    x = y;
                    y = tmpx;
                }
                region.recordX(x);
                region.recordY(y);
                tmp[0][step] = x;
                tmp[1][step] = y;
                tmp[2][step++] = value;
                if(step == 100000){
                    totalPos.add(tmp);
                    step = 0;
                    tmp = new int[3][100000];
                }
            }
            if(step != 0)
                totalPos.add(tmp);
            inputIterator.close();
            O.ptln("File read finished!");
            int minX = config.CutUseless ? region.minX : 0;
            int minY = config.CutUseless ? region.minY : 0;
            O.ptln(region.toString());
            Mat image = Mat.zeros(region.maxX + 1 - minX, region.maxY + 1 - minY, CvType.CV_16SC1);
            for(int[][] p : totalPos) {
                for(step = 0; step < 100000; step++){
                    x = p[0][step] - minX;
                    y = p[1][step] - minY;
                    value = p[2][step];
                    if(value == 0)
                        break;
                    image.put(x, y, image.get(x, y)[0] + value);
                }
            }
            O.ptln("Begin to normalize");
            for (x = 0; x < image.rows(); x++) {
                for (y = 0; y < image.cols(); y++) {
                    if (image.get(x, y)[0] > 1000) {
                        image.put(x, y, 999);
                    }
                    image.put(x, y, 255 * Math.tanh(Math.log10(image.get(x, y)[0] + 1)));
                }
            }
            Imgcodecs.imwrite(config.output_file, image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
