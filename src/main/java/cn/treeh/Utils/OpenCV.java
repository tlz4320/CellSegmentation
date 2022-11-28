package cn.treeh.Utils;

import org.opencv.core.Mat;

import java.util.Queue;

public class OpenCV {
    public static void paintLine(Mat original, int[] pos, int size) {
        int x, y;
        int channel = original.channels();
        double[] value;
        switch (channel) {
            case 1:
                value = new double[]{255};
                break;
            case 2:
                value = new double[]{255, 255};
                break;
            case 3:
                value = new double[]{255, 255, 255};
                break;
            default:
                value = new double[]{255, 255, 255, 255};
        }
        for (x = pos[0]; x < pos[1]; x++) {
            for (y = Math.max(pos[2] - size / 2, 0); y < Math.min(pos[2] + size / 2, original.cols()); y++)
                original.put(x, y, value);
            for (y = Math.max(pos[3] - size / 2, 0); y < Math.min(pos[3] + size / 2, original.cols()); y++)
                original.put(x, pos[3], value);
        }
        for (y = pos[2]; y < pos[3]; y++) {
            for (x = Math.max(0, pos[0] - size / 2); x < Math.min(pos[0] + size / 2, original.rows()); x++)
                original.put(x, y, value);
            for (x = Math.max(0, pos[1] - size / 2); x < Math.min(pos[1] + size / 2, original.rows()); x++)
                original.put(pos[1], y, value);
        }
    }
    public static void paintLines(Mat original, Queue<int[]> pos, int size){
        for(int[] p : pos){
            paintLine(original, p, size);
        }
    }
}
