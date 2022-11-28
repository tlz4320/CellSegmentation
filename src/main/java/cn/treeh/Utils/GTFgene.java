package cn.treeh.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GTFgene {
    public String getGeneID() {
        return GeneID;
    }

    String GeneID;

    public String getStrand() {
        return strand;
    }

    String strand;
    public HashMap<String, String> GeneInfo;
    public HashMap<String, LinkedList<GTFrecord>> Info;
    public HashMap<String, String> decodeInfo(String info){
        String[] sp = info.split("[;]");
        String[] sp2;
        HashMap<String, String> tempInfo = new HashMap<>();
        for(String s : sp){
            sp2 = s.trim().split("[ =]");
            tempInfo.put(sp2[0], sp2[1].replaceAll("\"",""));
        }
        return tempInfo;
    }
    public String getInfo(String key){
        return GeneInfo.get(key);
    }
    public LinkedList<GTFrecord> getAnnotation(String key){
        return Info.get(key);
    }
    public void addInfo(String type, Integer start, Integer end, Map<String, String> info){
        if(Info == null)
            Info = new HashMap<>();
        if(Info.containsKey(type))
            Info.get(type).add(new GTFrecord(start, end, info));
        else{
            LinkedList<GTFrecord> temp = new LinkedList<>();
            temp.add(new GTFrecord(start, end, info));
            Info.put(type, temp);
        }
    }
    public GTFgene(String line, String geneRecord){
        String[] sp = line.split("[\t]");
        GeneInfo = decodeInfo(sp[8]);
        GeneID = GeneInfo.get(geneRecord);
        strand = sp[6];
        addInfo(sp[2], Integer.parseInt(sp[3]), Integer.parseInt(sp[4]), GeneInfo);
    }
    public GTFgene(String line){
        this(line, "gene_id");
    }
    public GTFgene(HashMap<String, String> Info, String geneid, String type, Integer start, Integer end){
        this.GeneInfo = Info;
        GeneID = geneid;
        addInfo(type, start, end, GeneInfo);
    }
    public GTFgene(HashMap<String, String> Info, String geneid, String strand, String type, Integer start, Integer end){
        this.GeneInfo = Info;
        GeneID = geneid;
        this.strand = strand;
        addInfo(type, start, end, GeneInfo);
    }
    public GTFgene addInfo(String line){
        String[] sp = line.split("[\t]");
        HashMap<String, String> tempInfo = decodeInfo(sp[8]);
        String geneid = null;
        if(tempInfo.containsKey("gene_id"))
            geneid = tempInfo.get("gene_id");
        if(tempInfo.containsKey("geneid"))
            geneid = tempInfo.get("geneid");
        if(geneid == null)
            return null;
        if(GeneID.equals(geneid)) {
            addInfo(sp[2], Integer.parseInt(sp[3]), Integer.parseInt(sp[4]), tempInfo);
            return null;
        }
        return new GTFgene(tempInfo, geneid,sp[6], sp[2], Integer.parseInt(sp[3]), Integer.parseInt(sp[4]));
    }
}
