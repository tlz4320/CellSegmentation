package cn.treeh.Utils;

import cn.treeh.ToNX.O;

import java.io.BufferedReader;
import java.io.FileReader;

public class fastaReader {
    public static String readOneFile(String file){
        StringBuilder builder = new StringBuilder();
        try {

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            char firstchr;
            while((line = reader.readLine()) != null){
                firstchr = line.charAt(0);
                if((firstchr >= 'a' && firstchr <= 'z') || (firstchr >= 'A' && firstchr <= 'Z'))
                    builder.append(line.trim());
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
            O.ptln("fasta file error");
            throw new RuntimeException(e);
        }
        return builder.toString();
    }
}
