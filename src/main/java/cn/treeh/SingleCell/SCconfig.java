package cn.treeh.SingleCell;


import cn.treeh.ToNX.Annotation.Arg;
import cn.treeh.ToNX.util.ArgUtil;

public class SCconfig extends ArgUtil {
    @Arg(arg = "I", needed = true, hasArg = true, description = "input file")
    public String input_file;
    @Arg(arg = "O", needed = false, hasArg = true, description = "output file")
    public String output_file;
    @Arg(arg = "I2", needed = false, hasArg = true, description = "input file 2")
    public String input_file2;
    @Arg(arg = "O2", needed = false, hasArg = true, description = "output file 2")
    public String output_file2;
    @Arg(arg = "h",longarg = "help", needed = false,hasArg = false ,description = "", show = false)
    public boolean print_help;
    @Arg(arg = "t", longarg = "thread", needed = false, hasArg = true, description = "",val = "1", show = false)
    public int thread_num;
    @Arg(arg = "test",hasArg = false, val = "false", show = false)
    public boolean test;
    @Arg(arg = "I3", needed = false, hasArg = true, description = "input file 3")
    public String input_file3;
    @Arg(arg = "sep", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean SepCell;
    @Arg(arg = "sep2", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean SepCell2;
    @Arg(arg = "map", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean MapSep;
    @Arg(arg = "sample", needed = false, val = "10", description = "how many sample for using to define threshold", hasArg = true)
    public int sample;
    @Arg(arg = "sep3", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean SepCell3;
    @Arg(arg = "sep4", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean SepCell4;
    @Arg(arg = "sep5", needed = false, val = "false", description = "separate different cell", hasArg = false)
    public boolean SepCell5;
    @Arg(arg = "gen", needed = false, val = "false", description = "generate Image for ss data", hasArg = false)
    public boolean GenImg;
    @Arg(arg = "cut", needed = false, val = "false", description = "generate Image for ss data", hasArg = false)
    public boolean CutUseless;
    @Arg(arg = "exy", needed = false, val = "false", description = "generate Image for ss data", hasArg = false)
    public boolean ExchangeXY;
    @Arg(arg = "pca", needed = false, val = "false", description = "doPCA", hasArg = false)
    public boolean doPCA;
    @Arg(arg = "cvt", needed = false, val = "false", description = "cvtMatrix", hasArg = false)
    public boolean cvtMatrix;
    @Arg(arg = "cov", needed = false, val = "false", description = "coverage", hasArg = false)
    public boolean coverage;
    @Arg(arg = "cc", needed = false, val = "false", description = "coverage", hasArg = false)
    public boolean countCell;
    @Arg(arg = "cp", needed = false, val = "false", description = "coverage", hasArg = false)
    public boolean countPoint;
    @Arg(arg = "a", needed = false, val = "0.1", description = "alpha", hasArg = true)
    public double alpha;
    @Arg(arg = "b", needed = false, val = "1.5", description = "beta", hasArg = true)
    public double beta;
    @Arg(arg = "cell", needed = false, val = "3", description = "which column is cell ID", hasArg = true)
    public int cellCol;
    @Arg(arg = "gene", needed = false, val = "1", description = "which column is gene ID", hasArg = true)
    public int geneCol;
    @Arg(arg = "exp", needed = false, val = "5", description = "which region RNA segment use to expand", hasArg = true)
    public int expand;
    @Arg(arg = "merge", needed = false, val = "false", description = "Merge DNA and RNA image", hasArg = false)
    public boolean mergeDR;
    public int _expand = 1;
}