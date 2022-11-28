package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.E;
import cn.treeh.ToNX.O;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class MergeDR {
    public boolean checkColor(double[] c, int r, int g, int b){
        return c[0] == b && c[1] == g && c[2] == r;
    }
    void expand(Mat rna, Mat res, int x, int y){
        int top = y - 1, bottom = y + 1, left = x - 1, right = x + 1;
        boolean ctn, fnl;
        double[] colors;
        while(true) {
            ctn = false;
            fnl = false;
            for (int index = top; index <= bottom; index++) {
                colors = rna.get(left, index);
                if(checkColor(colors, 127, 0, 0)) {
                    fnl = true;
                    break;
                }
                colors = res.get(left, index);
                if(checkColor(colors, 0, 0, 0))
                    ctn = true;
                colors = rna.get(right, index);
                if(checkColor(colors, 127, 0, 0)){
                    fnl = true;
                    break;
                }
                colors = res.get(right, index);
                if(checkColor(colors, 0, 0, 0))
                    ctn = true;
            }
            for (int index = left; index <= right; index++) {
                colors = rna.get(index, top);
                if(checkColor(colors, 127, 0, 0)){
                    fnl = true;
                    break;
                }
                colors = rna.get(index, top);
                if(checkColor(colors, 0, 0, 0))
                    ctn = true;
                colors = rna.get(index, bottom);
                if(checkColor(colors, 127, 0, 0)){
                    fnl = true;
                    break;
                }
                colors = rna.get(index, bottom);
                if(checkColor(colors, 0, 0, 0))
                    ctn = true;
            }
            if(ctn){
                for (int index = top; index <= bottom; index++) {
                    res.put(left, index, 181,228,255);
                    res.put(right, index, 181,228,255);
                }
                for (int index = left; index <= right; index++) {
                    res.put(index, top, 181,228,255);
                    res.put(index, bottom, 181,228,255);
                }
            }
            else
                break;
            if(fnl)
                return;
            top = Math.max(0, top - 1);
            bottom = Math.min(res.rows(), bottom + 1);
            left = Math.max(0, left - 1);
            right = Math.min(res.cols(), right + 1);
        }
    }
    public void main(SCconfig config) {
        System.loadLibrary("opencv_java452");
        Mat ssDNA = Imgcodecs.imread(config.input_file);
        Mat RNA = Imgcodecs.imread(config.input_file2);
        int rows = ssDNA.rows();
        int cols = ssDNA.cols();
        if (rows != RNA.rows() || cols != RNA.cols()) {
            E.ptln("Input images don't have same size!");
            return;
        }
        O.ptln("Begin to read file");
        Mat result = Mat.zeros(rows, cols, ssDNA.type());
        double[] rna, dna;
        for(int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                rna = RNA.get(x, y);
                if (!checkColor(rna, 0, 0, 0) && !checkColor(rna, 127, 0, 0)) {
                    expand(RNA, result, x, y);
                }
            }
        }
        Imgcodecs.imwrite(config.output_file + "RNA.png", result);
        O.ptln("RNA read finished");
        for(int x = 0; x < rows; x++){
            for(int y = 0; y < cols; y++){
                dna = ssDNA.get(x, y);
                if(!checkColor(dna, 0, 0, 0)) {
                    result.put(x, y, 204, 50, 153);
                }
            }
        }
        O.ptln("Merge finished! Writing..");
        Imgcodecs.imwrite(config.output_file, result);
    }
}
