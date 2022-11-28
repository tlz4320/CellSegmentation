package cn.treeh.SingleCell;

import cn.treeh.SingleCell.SS.*;
import cn.treeh.ToNX.O;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.LinkedList;


public class SCMain {
    public void main(String[] args) {
        SCconfig config = new SCconfig();
        int level = config.parseLevel(args, config);
        O.ptln("parameter parse finished");
        O.ptln(config.toString());
        if (config.SepCell){
            O.ptln("Begin to Sep");
            System.loadLibrary("opencv_java452");
            ssNuclearSep sep = new ssNuclearSep();
//            sep.main(config);
            ssRNASep sepRNA = new ssRNASep();
            sepRNA.main(sep.main(config), config);
        }

    }
}
