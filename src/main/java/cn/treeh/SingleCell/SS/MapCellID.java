package cn.treeh.SingleCell.SS;

import cn.treeh.SingleCell.SCconfig;
import cn.treeh.ToNX.Iterator.InputCreator;
import cn.treeh.ToNX.Iterator.InputIterator;
import cn.treeh.ToNX.O;

import java.io.FileWriter;
import java.util.TreeMap;

public class MapCellID {
    public void main(SCconfig config) {
        try {
            TreeMap<Long, Integer> cellID = new TreeMap<>();
            InputIterator inputIterator = InputCreator.openInputIterator(config.input_file);
            long pos;
            int id;
            String[] tmp;
//            41875  第二列加上18325
            while(inputIterator.hasNext()){
                tmp = inputIterator.next();
                pos = (Long.parseLong(tmp[0])) * 1000000 + Long.parseLong(tmp[1]);
                id = Integer.parseInt(tmp[2]);
                cellID.put(pos, id);
            }
            inputIterator.close();
            O.ptln("result read finished");
            inputIterator = InputCreator.openInputIterator(config.input_file2);
            while(inputIterator.next()[0].startsWith("#"));
            FileWriter writer = new FileWriter(config.output_file);
            while(inputIterator.hasNext()){
                tmp = inputIterator.next();
                pos = (Long.parseLong(tmp[2])) * 1000000 + Long.parseLong(tmp[1]);
                if(cellID.containsKey(pos))
                    writer.write(inputIterator.getThisLine() + "\t" + cellID.get(pos) + "\n");
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
