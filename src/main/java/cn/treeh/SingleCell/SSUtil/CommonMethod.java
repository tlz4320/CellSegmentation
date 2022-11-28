package cn.treeh.SingleCell.SSUtil;

import cn.treeh.SingleCell.SS.Cell;
import cn.treeh.SingleCell.SS.Processor;

import java.util.LinkedList;

public class CommonMethod {
    public static void splitCell(LinkedList<Processor> total_region){
        for(Processor p : total_region){
            LinkedList<Cell> newCell = new LinkedList<>(), oldCell = new LinkedList<>();
            for(Cell c : p.getCells()){
                if(c.getCellRegion().width() > 60 || c.getCellRegion().height() > 60){
                    p.splitCell(c, newCell);
                    oldCell.add(c);
                }
            }
            if(newCell.size() != 0){
                p.getCells().addAll(newCell);
            }
            p.getCells().removeAll(oldCell);
        }
    }
}
