package cn.treeh.SingleCell.SS;

import cn.treeh.ToNX.O;
import cn.treeh.Utils.MMath;
import cn.treeh.Utils.Point;
import cn.treeh.Utils.Region;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.*;

public class CutImg {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java452");
        Mat m = Imgcodecs.imread("d://HCC-28-PT-E6-T10-T1_Bin50.fill.boundaryline.png");
        Mat original = m.clone();//复制一份原图 因为m后面被我修改了
        LinkedList<Point> edge = new LinkedList<>();
        double[] bgr;
        //把黑色的点找出来 做给分割线
        for (int x = 0; x < m.rows(); x++) {
            for (int y = 0; y < m.cols(); y++) {
                bgr = m.get(x, y);
                if (bgr[0] == 0 && bgr[1] == 0 && bgr[2] == 0)
                    edge.add(new Point(x, y));
            }
        }
        CutImg cutImg = new CutImg();
        //找到组织边界  如果存在不连通的情况 则只保留最大连通图形
        LinkedList<Point> ll = cutImg.defineRegion(m);
        //让分割线能够分割组织边界 所以要让分割线延长并且搭在组织边界上  这部分可能会有问题
        //可能会找不准 所以最好在手动划分的时候就保证边界线和组织线是有交集的
        cutImg.combineEdges(ll, edge);
        //链接不连续的边界线，稍微断几个像素是没事的 如果断太厉害了  估计也会出错
        LinkedList<Point> edge2 = cutImg.fixPoints(edge);
        for (Point p : edge) {
            m.put(p.x, p.y, 255, 255, 255);
        }
        //根据组织线把组织分割成两部分
        HashSet<LinkedList<Point>> splitTissue = cutImg.splitTissueRegion(ll, edge2, original);
        for (LinkedList<Point> tissue : splitTissue) {
            //每个部分都从边界线开始扩张
            tissue = cutImg.expand(tissue, edge2, m);
            for (Point p : tissue) {
                original.put(p.x, p.y, 0, 0, 0);
            }
        }
        Imgcodecs.imwrite("d://LQL.png",original);
        O.ptln("test");
    }
    public double[] leastSquare(LinkedList<Point> points) {
        double a1 = 0, a2 = 0, b = 0, a;
        double meanX = 0, meanXY = 0, meanY = 0, meanXX = 0;
        for (Point p : points) {
            meanX += p.x;
            meanY += p.y;
            meanXX += p.x * p.x;
            meanXY += p.x * p.y;
        }
        meanXX /= points.size();
        meanXY /= points.size();
        meanX /= points.size();
        meanY /= points.size();
        a1 = meanXY - meanX * meanY;
        a2 = meanXX - meanX * meanX;
        if (a2 == 0) {
            a = Double.MAX_VALUE;
            return new double[]{a, 0, meanX, meanY};
        }
        a = a1 / a2;
        b = meanY - a * meanX;
        return new double[]{a, b, meanX, meanY};
    }

    public Point findMostDisPoint(LinkedList<Point> points, double[] dir, boolean left, boolean up) {
        double x = 0;
        Point res = null;
        if (dir[0] == Double.MAX_VALUE) {
            if (left) {
                x = Integer.MAX_VALUE;
                for (Point p : points) {
                    if (p.x < x) {
                        x = p.x;
                        res = p;
                    }
                }
            } else {
                x = Integer.MIN_VALUE;
                for (Point p : points) {
                    if (p.x > x) {
                        x = p.x;
                        res = p;
                    }
                }
            }
            return res;
        }
        if (left && up) {//left
            x = 0;
            for (Point p : points) {
                if ((p.y - dir[1]) / dir[0] - p.x > x) {
                    x = (p.y - dir[1]) / dir[0] - p.x;
                    res = p;
                }
            }
            return res;
        } else if (!left && up) { //right
            x = 0;
            for (Point p : points) {
                if (p.x - (p.y - dir[1]) / dir[0] > x) {
                    x = p.x - (p.y - dir[1]) / dir[0];
                    res = p;
                }
            }
            return res;
        }
        if (left) { //up
            x = 0;
            for (Point p : points) {
                if (p.y - (dir[0] * p.x + dir[1]) > x) {
                    x = p.y - (dir[0] * p.x + dir[1]);
                    res = p;
                }
            }

        } else { // down
            x = 0;
            for (Point p : points) {
                if (dir[0] * p.x + dir[1] - p.y > x) {
                    x = dir[0] * p.x + dir[1] - p.y;
                    res = p;
                }
            }
        }
        return res;
    }
    public Point findValidTissuePoint(Mat img, Point p, double[] dir, boolean left, boolean up){
        double[] bgr;
        if(left && up){
            int y = (int)dir[3];

            for(int x = 0; x < (y - p.y) / dir[0] + p.x; x++){
                bgr = img.get(x, y);
                if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                    return new Point(x, y);
                }
            }

        }
        else if(!left && up) {
            int y = (int) dir[3];

            for (int x = (int) Math.ceil((y - p.y) / dir[0] + p.x); x < img.rows(); x++) {
                bgr = img.get(x, y);
                if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                    return new Point(x, y);
                }
            }

        }
        else if(left) {
            int x = (int) dir[2];

            for (int y = (int) Math.ceil((x - p.x) * dir[0] + p.y); y < img.cols(); y++) {
                bgr = img.get(x, y);
                if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                    return new Point(x, y);
                }

            }
        }
        else {
            int x = (int) dir[2];
            for (int y = 0; y < (x - p.x) * dir[0] + p.y; y++) {
                bgr = img.get(x, y);
                if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                    return new Point(x, y);
                }
            }

        }
        return null;
    }
    public Point expand(LinkedList<Point> points, Mat img, boolean left, boolean up, double[] dir) {
        Point tmpp;
        if(left && up){
            tmpp = findMostDisPoint(points, dir, left, up);
            tmpp.x--;
            img = splitTissue(img, findValidTissuePoint(img, tmpp, dir, left, up));
        }
        else if(!left && up){
            tmpp = findMostDisPoint(points, dir, left, up);
            tmpp.x++;
            img = splitTissue(img, findValidTissuePoint(img, tmpp, dir, left, up));
        }
        else if(left){
            tmpp = findMostDisPoint(points, dir, left, up);
            tmpp.y++;
            img = splitTissue(img, findValidTissuePoint(img, tmpp, dir, left, up));
        }
        else{
            tmpp = findMostDisPoint(points, dir, left, up);
            tmpp.y--;
            img = splitTissue(img, findValidTissuePoint(img, tmpp, dir, left, up));
        }
        int totalPoint = 0;
        double[] bgr;
        Region region = new Region();
        for(int x = 0; x < img.rows(); x++){
            for(int y = 0; y < img.cols(); y++){
                bgr = img.get(x, y);
                if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                    totalPoint++;
                    region.recordX(x);
                    region.recordY(y);
                }
            }
        }
        int acc = 0;
        Point last, now;
        if(left && up){
            last = findMostDisPoint(points, dir, false, up);
            while(acc < 0.5 * totalPoint){
                now = new Point(last.x - 5, last.y);
                for(int y = region.minY; y < region.maxY; y++){
                    for(int x = (int)(Math.floor((y - now.y) / dir[0] + now.x));
                    x <= (int)(Math.ceil((y - last.y) / dir[0] + last.x)); x++){
                        if(x < 0 || x > region.maxX)
                            break;
                        bgr = img.get(x, y);
                        if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                            acc++;
                            img.put(x, y, 255, 255, 255);
                        }
                    }
                }
                last = now;
            }
        }
        else if(!left && up){
            last = findMostDisPoint(points, dir, true, up);
            while(acc < 0.5 * totalPoint){
                now = new Point(last.x + 5, last.y);
                for(int y = region.minY; y < region.maxY; y++){
                    for(int x = (int)(Math.ceil((y - now.y) / dir[0] + now.x));
                        x >= (int)(Math.floor((y - last.y) / dir[0] + last.x)); x--){
                        if(x < 0 || x > region.maxX)
                            break;
                        bgr = img.get(x, y);
                        if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                            acc++;
                            img.put(x, y, 255, 255, 255);
                        }
                    }
                }
                last = now;
            }
        }
        if(left){
            last = findMostDisPoint(points, dir, false, up);
            while(acc < 0.5 * totalPoint){
                now = new Point(last.x, last.y + 5);
                for(int x = region.minX; x < region.maxX; x++){
                    for(int y = (int)(Math.ceil((x - now.x) * dir[0] + now.y));
                        y >= (int)(Math.floor((x - last.x) * dir[0] + last.y)); y--){
                        if(y < 0 || y > region.maxY)
                            break;
                        bgr = img.get(x, y);
                        if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                            acc++;
                            img.put(x, y, 255, 255, 255);
                        }
                    }
                }
                last = now;
            }
        }
        else{
            last = findMostDisPoint(points, dir, false, up);
            while(acc < 0.5 * totalPoint){
                now = new Point(last.x, last.y - 5);
                for(int x = region.minX; x < region.maxX; x++){
                    for(int y = (int)(Math.floor((x - now.x) * dir[0] + now.y));
                        y <= (int)(Math.ceil((x - last.x) * dir[0] + last.y)); y++){
                        if(y < 0 || y > region.maxY)
                            break;
                        bgr = img.get(x, y);
                        if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                            acc++;
                            img.put(x, y, 255, 255, 255);
                        }
                    }
                }
                last = now;
            }
        }
        return last;
    }
    public LinkedList<Point> expand(LinkedList<Point> points, Mat m, double[] dir){
        LinkedList<Point> res = new LinkedList<>();
        if(Math.abs(dir[0]) < 1){
            res.add(expand(points, m, true, false, dir));
            res.add(expand(points, m, false, false, dir));
        }
        else{
            res.add(expand(points, m, true, true, dir));
            res.add(expand(points, m, false, true, dir));
        }
        return res;
    }
    public double Dis(Point p1, Point p2) {
        if (Math.abs(p1.x - p2.x) <= 1 && Math.abs(p1.y - p2.y) <= 1)
            return 1;
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public void colorLink(int[][] edge, Point p, int id) {
        LinkedList<Point> left = new LinkedList<>();
        left.add(p);
        edge[p.x][p.y] = id;
        int tmpx, tmpy;
        while (!left.isEmpty()) {
            p = left.pop();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    tmpx = MMath.ceil_floor(0, edge.length, p.x + x);
                    tmpy = MMath.ceil_floor(0, edge[0].length, p.y + y);
                    if (edge[tmpx][tmpy] == -1) {
                        edge[tmpx][tmpy] = id;
                        left.add(new Point(tmpx, tmpy));
                    }
                }
            }
        }
    }

    public Point findUncover(int[][] edge) {
        for(int x = 0; x < edge.length; x++){
            for(int y = 0; y < edge[x].length; y++){
                if(edge[x][y] == -1)
                    return new Point(x, y);
            }
        }
        return null;
    }
    public HashMap<Integer, LinkedList<Point>> splitLink(int[][] edge, int id){
        HashMap<Integer, LinkedList<Point>> res = new HashMap<>();
        for(int index = 0; index < id; index++){
            res.put(index + 1, new LinkedList<>());
        }
        for(int x = 0; x < edge.length; x++){
            for(int y = 0; y < edge[x].length; y++){
                if(edge[x][y] > 0)
                    res.get(edge[x][y]).add(new Point(x, y));
            }
        }
        return res;
    }
    public Point[] findClosetPoint(LinkedList<Point> p1, LinkedList<Point> p2){
        double dis = 999999, tmp;
        Point[] res = new Point[2];
        for(Point pp1 : p1){
            for(Point pp2 : p2){
                tmp = Dis(pp1, pp2);
                if(tmp < dis){
                    dis = tmp;
                    res[0] = pp1;
                    res[1] = pp2;
                }
            }
        }
        return res;
    }
    public void LinkEdge(Point p, Point cp, LinkedList<Point> res, Region region){
        int minx, maxx, miny, maxy;
        minx = Math.min(p.x + region.minX, cp.x + region.minX);
        maxx = Math.max(p.x + region.minX, cp.x + region.minX);
        miny = Math.min(p.y + region.minY, cp.y + region.minY);
        maxy = Math.max(p.y + region.minY, cp.y + region.minY);
        for (int x = minx + 1; x <= maxx; x++) {
            res.add(new Point(x, miny));
        }
        for (int y = miny + 1; y < maxy; y++) {
            res.add(new Point((p.x == minx ^ p.y == miny) ? maxy : minx, y));
        }
    }
    public LinkedList<Point> fixPoints(LinkedList<Point> points) {
        LinkedList<Point> res = new LinkedList<>(points);
        double dis, tmp;
        Point cp = null;
        Region region = new Region();
        for (Point p : points) {
            region.recordX(p.x);
            region.recordY(p.y);
        }
        int[][] edge = new int[region.width() + 1][region.height() + 1];
        for (Point p : points) {
            edge[p.x - region.minX][p.y - region.minY] = -1;
        }
        int id = 0;
        while (true) {
            Point p = findUncover(edge);
            if(p == null)
                break;
            colorLink(edge, p, ++id);
        }
        HashMap<Integer, LinkedList<Point>> splitLink = splitLink(edge, id);
        HashSet<Integer> linked = new HashSet<>();
        linked.add(1);
        HashSet<Integer> unlinked = new HashSet<>();
        for(int id1 = 2; id1 <= id; id1++){
            unlinked.add(id1);
        }
        while(!unlinked.isEmpty()){
            double tmpdis = 999, tmptmp;
            Point[] tmpmax = null;
            int tmpid2 = 0;
            for(int id1 : linked){
                for(int id2 : unlinked){
                    Point[] tmpres = findClosetPoint(splitLink.get(id1), splitLink.get(id2));
                    tmptmp = Dis(tmpres[0], tmpres[1]);
                    if(tmptmp < tmpdis){
                        tmpdis = tmptmp;
                        tmpmax = tmpres;
                        tmpid2 = id2;
                    }
                }
            }
            unlinked.remove(tmpid2);
            linked.add(tmpid2);
            LinkEdge(tmpmax[0], tmpmax[1], res, region);
        }
        return res;
    }

    public Mat splitTissue(Mat m, Point p) {
        Mat res = Mat.zeros(m.rows(), m.cols(), m.type());
        for(int x = 0; x < m.rows(); x++){
            for(int y = 0; y < m.cols(); y++)
                res.put(x, y, 255, 255, 255);
        }
        LinkedList<Point> tmp = new LinkedList<>();
        p.v = m.get(p.x, p.y);
        tmp.add(p);
        int tmpx, tmpy;
        double[] bgr;
        while (!tmp.isEmpty()) {
            p = tmp.pop();
            res.put(p.x, p.y, p.v);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if(Math.abs(x) == 1 && Math.abs(y) == 1)
                        continue;
                    tmpx = MMath.ceil_floor(0, m.rows() - 1, p.x + x);
                    tmpy = MMath.ceil_floor(0, m.cols() - 1, p.y + y);
                    bgr = m.get(tmpx, tmpy);
                    if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                        tmp.add(new Point(tmpx, tmpy, m.get(tmpx, tmpy)));
                    }
                    m.put(tmpx, tmpy, 255, 255, 255);
                }
            }
        }
        return res;
    }
    public LinkedList<Point> expand(LinkedList<Point> tissue, LinkedList<Point> edge, Mat m){
        Mat tmp = Mat.zeros(m.rows(), m.cols(), m.type());
        for(int x = 0; x < m.rows(); x++){
            for(int y = 0; y < m.cols(); y++)
                tmp.put(x, y, 255, 255, 255);
        }
        LinkedList<Point> tmpT = new LinkedList<>(tissue);
        Point p;
        int tmpx, tmpy;
        double[] bgr;
        int totalPoint = 0;
        while(tmpT.size() != 0){
            p = tmpT.pop();
            totalPoint++;
            if(p.v != null)
                tmp.put(p.x, p.y, p.v);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if(Math.abs(x) == 1 && Math.abs(y) == 1)
                        continue;
                    tmpx = MMath.ceil_floor(0, m.rows() - 1, p.x + x);
                    tmpy = MMath.ceil_floor(0, m.cols() - 1, p.y + y);
                    bgr = m.get(tmpx, tmpy);
                    if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                        tmpT.add(new Point(tmpx, tmpy, bgr));
                    }
                    m.put(tmpx, tmpy, 255, 255, 255);
                }
            }
        }
        tmpT.addAll(edge);
        int acc = 1;
        finalLoop: while(tmpT.size() != 0){
            p = tmpT.pop();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    tmpx = MMath.ceil_floor(0, m.rows() - 1, p.x + x);
                    tmpy = MMath.ceil_floor(0, m.cols() - 1, p.y + y);
                    bgr = tmp.get(tmpx, tmpy);
                    tmp.put(tmpx, tmpy, 255, 255, 255);
                    if (bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255) {
                        tmpT.add(new Point(tmpx, tmpy));
                        acc++;
                        if(acc >= (totalPoint / 2))
                            break finalLoop;
                    }
                }
            }
        }
        return tmpT;
    }

//        double[] dir = cutImg.leastSquare(edge2);
//        LinkedList<Point> result = cutImg.expand(edge2, m, dir);

//        Mat m2 = Mat.zeros(m.rows(), m.cols(), CvType.CV_8SC3);
//        for(int x = 0; x < m.rows(); x++) {
//            for(int y = 0; y < m.cols(); y++){
//                m2.put(x, y, 255, 255,255);
//            }
//        }
//        for(Point p : result){
//            cutImg.plotLine(original, p, dir);
//        }

    public HashSet<LinkedList<Point>> splitTissueRegion(LinkedList<Point> tissue, LinkedList<Point> edge, Mat m){
        HashSet<LinkedList<Point>> res = new HashSet<>();
        m = Mat.zeros(m.rows(), m.cols(), CvType.CV_8SC1);
        for(int x = 0; x < m.rows(); x++){
            for(int y = 0; y < m.cols(); y++){
                m.put(x, y, 0);
            }
        }
        for(Point p : tissue){
            m.put(p.x, p.y, 2);
        }
        for(Point p : edge){
            m.put(p.x, p.y, 0);
        }
        int acc = 0;
        while(acc++ < 2) {
            LinkedList<Point> tmp = new LinkedList<>();
            for (int x = 0; x < m.rows(); x++) {
                for (int y = 0; y < m.cols(); y++) {
                    if (m.get(x, y)[0] != 0) {
                        tmp.add(new Point(x, y));
                        break;
                    }
                }
            }
            Point p;
            LinkedList<Point> tmpres = new LinkedList<>();
            int tmpx, tmpy;
            while (!tmp.isEmpty()) {
                p = tmp.pop();
                tmpres.add(p);
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        tmpx = MMath.ceil_floor(0, m.rows() - 1, p.x + x);
                        tmpy = MMath.ceil_floor(0, m.cols() - 1, p.y + y);
                        if (m.get(tmpx, tmpy)[0] != 0)
                            tmp.add(new Point(tmpx, tmpy));
                        m.put(tmpx, tmpy, 0);
                    }
                }
            }
            res.add(tmpres);
        }
        return res;
    }
    public void plotLine(Mat m, Point p, double[] dir) {
        int x, y;
        for (x = 0; x < m.rows(); x++) {
            y = (int) (dir[0] * (x - p.x) + p.y);
            if (y >= 0 && y < m.cols())
                m.put(x, y, 0, 0, 0);
        }
        for (y = 0; y < m.cols(); y++) {
            x = (int) ((y - p.y) / dir[0] + p.x);
            if (x >= 0 && x < m.rows())
                m.put(x, y, 0, 0, 0);
        }
    }
    public void combineEdges(LinkedList<Point> tissue, LinkedList<Point> edge){
        double maxDis = 0;
        for(Point p : edge){
            for(Point p2 : edge){
                maxDis = Math.max(maxDis, Dis(p, p2));
            }
        }
        ArrayList<Point> recordClosePoint = new ArrayList<>();
        double dis;
        for(Point p1 : tissue){
            p1.v = new double[]{999};
            for(Point p2 : edge){
                dis = Dis(p1, p2);
                if(dis < 4) {
                    p1.v[0] = Math.min(p1.v[0], dis);
                }
            }
            if(p1.v[0] < 4)
                recordClosePoint.add(p1);
        }
        recordClosePoint.sort(new Comparator<Point>() {
            @Override
            public int compare(Point point, Point t1) {
                return point.v[0] < t1.v[0] ? -1 : 1;
            }
        });
        boolean flag = true;
        Point first = null, second = null;
        for(Point e : recordClosePoint){
            if(flag){
                flag = false;
                first = e;
            }
            else{
                dis = Dis(e, first);
                if(dis > maxDis / 2) {
                    second = e;
                    break;
                }
            }
        }
        edge.add(first);
        edge.add(second);

    }
    public boolean isEdge(Mat m, Point p) {
        int tmpx, tmpy;
        double[] bgr;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                tmpx = MMath.ceil_floor(0, m.rows() - 1, p.x + x);
                tmpy = MMath.ceil_floor(0, m.cols() - 1, p.y + y);
                bgr = m.get(tmpx, tmpy);
                if (bgr[0] == 255 && bgr[1] == 255 && bgr[2] == 255) {
                    return true;
                }
            }
        }
        return false;
    }
    public LinkedList<Point> findLink(Mat m, Point p, Mat original){
        LinkedList<Point> res = new LinkedList<>();
        LinkedList<Point> tmp = new LinkedList<>();
        tmp.add(p);
        int[][] record = new int[m.rows()][m.cols()];
        double[] bgr;
        int tmpx, tmpy;
        while(!tmp.isEmpty()){
            p = tmp.pop();
            if(isEdge(original, p))
                res.add(p);
            else
                continue;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    tmpx = MMath.ceil_floor(0, record.length - 1, p.x + x);
                    tmpy = MMath.ceil_floor(0, record[0].length - 1, p.y + y);
                    bgr = m.get(tmpx, tmpy);
                    if(bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255){
                        tmp.add(new Point(tmpx, tmpy));
                    }
                    m.put(tmpx, tmpy, 255, 255,255);
                }
            }
        }
        tmp.addAll(res);
        while(!tmp.isEmpty()){
            p = tmp.pop();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    tmpx = MMath.ceil_floor(0, record.length - 1, p.x + x);
                    tmpy = MMath.ceil_floor(0, record[0].length - 1, p.y + y);
                    bgr = m.get(tmpx, tmpy);
                    if(bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255){
                        tmp.add(new Point(tmpx, tmpy));
                    }
                    m.put(tmpx, tmpy, 255, 255,255);
                }
            }
        }
        return res;
    }
    public LinkedList<Point> defineRegion(Mat m){
        double[] bgr;
        LinkedList<Point> tissueEdge = new LinkedList<>();
        Mat record = m.clone();
        int maxSize = 0;
        for(int x = 0; x < m.rows(); x++) {
            for(int y = 0; y < m.cols(); y++){
                bgr = m.get(x,y);
                if(bgr[0] != 255 || bgr[1] != 255 || bgr[2] != 255){
                    LinkedList<Point> tmp = findLink(record, new Point(x, y), m);
                    if(tmp.size() > maxSize){
                        maxSize = tmp.size();
                        tissueEdge = tmp;
                    }
                }
            }
        }
        return tissueEdge;
    }
}
