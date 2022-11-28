package cn.treeh.Utils;

public class ReverseComplement {

        static char[] DNANucleotide = new char[512];
        static char[] RNANucleotide = new char[512];
        static {
            DNANucleotide['A'] = 'T';
            DNANucleotide['T'] = 'A';
            DNANucleotide['C'] = 'G';
            DNANucleotide['G'] = 'C';
            DNANucleotide['a'] = 't';
            DNANucleotide['t'] = 'a';
            DNANucleotide['c'] = 'g';
            DNANucleotide['g'] = 'c';
            RNANucleotide['A'] = 'U';
            RNANucleotide['U'] = 'A';
            RNANucleotide['C'] = 'G';
            RNANucleotide['G'] = 'C';
            RNANucleotide['a'] = 'u';
            RNANucleotide['u'] = 'a';
            RNANucleotide['c'] = 'g';
            RNANucleotide['g'] = 'c';
            DNANucleotide['N'] = 'N';
            DNANucleotide['n'] = 'n';
        }

    public static String reverseComplement(String seq, boolean isDNA){
        char []mapping = isDNA ? DNANucleotide : RNANucleotide;
        byte[] bytes = seq.getBytes();
        StringBuilder builder = new StringBuilder();
        for(int i = bytes.length - 1; i >= 0; i--)
            builder.append(mapping[bytes[i]]);
        return builder.toString();
    }

}
