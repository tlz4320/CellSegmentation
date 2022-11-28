package cn.treeh.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class GTFInfo {
    HashMap<String, PairWiseLinker<GTFgene>> geneInfo = new HashMap<>();

    public HashMap<String, PairWiseLinker<GTFgene>> getGeneInfo() {
        return geneInfo;
    }
    public void putGeneInfo(String line, String geneRecord) {
        String[] sp = line.split("[\t]");
        if (sp.length < 9)
            return;
        PairWiseLinker<GTFgene> tempList;
        if (geneInfo.containsKey(sp[0]))
            tempList = geneInfo.get(sp[0]);
        else {
            tempList = new PairWiseLinker<GTFgene>();
            geneInfo.put(sp[0], tempList);
        }

        if (tempList.tail == null) {
            tempList.add(new GTFgene(line, geneRecord));
            return;
        }
        GTFgene gene = tempList.tail.object.addInfo(line);
        if (gene != null)
            tempList.add(gene);

    }
    public void putGeneInfo(String line){
        String[] sp = line.split("[\t]");
        if(sp.length < 9)
            return;
        PairWiseLinker<GTFgene> tempList;
        if(geneInfo.containsKey(sp[0]))
            tempList = geneInfo.get(sp[0]);
        else{
            tempList = new PairWiseLinker<GTFgene>();
            geneInfo.put(sp[0], tempList);
        }

        if(tempList.tail == null){
            tempList.add(new GTFgene(line));
            return;
        }
        GTFgene gene = tempList.tail.object.addInfo(line);
        if(gene != null)
            tempList.add(gene);
    }
    public static GTFInfo readsGTF(String path, String geneRecord){
        GTFInfo gtfInfo = new GTFInfo();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                if(line.charAt(0) == '#')
                    continue;
                gtfInfo.putGeneInfo(line, geneRecord);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return gtfInfo;
    }
    public static GTFInfo readsGTF(String path){
        return readsGTF(path, "gene_id");
    }
}
