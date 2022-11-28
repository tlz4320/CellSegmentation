package cn.treeh.SingleCell.SS;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SscSep {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java452");
        try {
//            InputIterator inputIterator = InputCreator.openInputIterator(config.input_file);
//            String[] temp;
//            int maxX = 0, maxY = 0, minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
//            int x, y, value;
//            while (inputIterator.hasNext()) {
//                temp = inputIterator.next();
//                if (temp[1].toLowerCase(Locale.ROOT).equals("x")) {
//                    continue;
//                }
//                x = Integer.parseInt(temp[1]);
//                y = Integer.parseInt(temp[2]);
//                maxX = Math.max(x, maxX);
//                maxY = Math.max(y, maxY);
//                minX = Math.min(x, minX);
//                minY = Math.min(y, minY);
//            }
//            inputIterator.close();
//            O.ptln("minX:" + minX + "\t" + "minY:" + minY);
//            Mat image = Mat.zeros(maxX - minX + 1, maxY - minY + 1, CvType.CV_16SC1);
//            inputIterator = InputCreator.openInputIterator(config.input_file);
//            double pixelValue = 0, maxValue = 0;
//            while (inputIterator.hasNext()) {
//                temp = inputIterator.next();
//                if (temp[1].toLowerCase(Locale.ROOT).equals("x")) {
//                    continue;
//                }
//                x = Integer.parseInt(temp[1]);
//                y = Integer.parseInt(temp[2]);
//                value = Integer.parseInt(temp[3]);
//                pixelValue = image.get(x - minX, y - minY)[0] + value;
//                maxValue = Math.max(pixelValue, maxValue);
//                image.put(x - minX, y - minY, pixelValue);
//            }
//            inputIterator.close();
//            Mat original = new Mat();
//            image.copyTo(original);
//            for (x = 0; x < image.rows(); x++) {
//                for (y = 0; y < image.cols(); y++) {
//                    if (image.get(x, y)[0] > 1000) {
//                        image.put(x, y, 999);
//                    }
//                    image.put(x, y, 255 * Math.tanh(Math.log10(image.get(x, y)[0] + 1)));
//                }
//            }
//            Imgcodecs.imwrite("d://M_trans.png", image);
//            Imgcodecs.imwrite("d://M_original.png", original);
            Mat image = Imgcodecs.imread("D:\\T33_segment\\P1.png");
            Mat gsBlur = new Mat();
            Mat gsBlur_filtered = new Mat();
            Imgproc.GaussianBlur(image, gsBlur, new Size(51, 51), 0, 0);
            Imgcodecs.imwrite("D:\\T33_segment\\P1_gus.png", gsBlur);
//
            Imgproc.threshold(gsBlur, gsBlur_filtered, 50, 255, Imgproc.THRESH_BINARY);
            Imgcodecs.imwrite("D:\\T33_segment\\P1_gus_filter.png", gsBlur_filtered);
//            Mat laplacian = new Mat();
//            Mat laplacian_binary = new Mat();
//            Imgproc.Laplacian(gsBlur_filtered, laplacian, CvType.CV_16SC1, 3, 1, 0);
//            Imgproc.threshold(laplacian, laplacian_binary, 50, 255, Imgproc.THRESH_BINARY);
//            Imgcodecs.imwrite("d://laplacian.png", laplacian_binary);
//            LinkedLi  st<Mat> list = new LinkedList<>();
//            list.add(Mat.zeros(image.rows(), image.cols(), CvType.CV_16SC1));
//            list.add(image);
//            list.add(gsBlur_filtered);
//
//            Mat out = new Mat();
//            Core.merge(list, out);
//            Imgcodecs.imwrite("d://merged2.png", out);
//            average = average / (image.rows() * image.cols());
//            O.ptln("convert to Image size:" + image.size());
//            int[] imageRange = subtleTrim(image, roughTrim(image, 100, 20), 100, 0);
//            O.ptln("max expression value is: " + maxValue);
//            O.ptln(imageRange[0] + "," + imageRange[1] + "," + imageRange[2] + "," + imageRange[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
