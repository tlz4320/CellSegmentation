package cn.treeh.Utils;

public class MSmithWaterman{
    int[][] scores;
    int[][][] step;

    public int getMismatch() {
        return mismatch;
    }

    public void setMismatch(int mismatch) {
        this.mismatch = mismatch;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getGapExt() {
        return gapExt;
    }

    public void setGapExt(int gapExt) {
        this.gapExt = gapExt;
    }

    public int getAllMismatch() {
        return allMismatch;
    }

    public void setAllMismatch(int allMismatch) {
        this.allMismatch = allMismatch;
    }

    public int getAllGap() {
        return allGap;
    }

    public void setAllGap(int allGap) {
        this.allGap = allGap;
    }
    public int getFinalScore(){
        return scores[len1][len2];
    }
    int mismatch = -2, match = 3, gap = -3, gapExt = -4, allMismatch = 3, allGap = 2;
    public String queryRes, targetRes;
    public int queryPos;
    int maxLen, targetLen, len1, len2;
    public MSmithWaterman(int maxLen, int targetLen){
        scores = new int[maxLen + 1][targetLen + 1];
        step = new int[maxLen + 1][targetLen + 1][3];
        this.maxLen = maxLen;
        this.targetLen = targetLen;
    }
    void reset(){
        for(int i = 0; i <= maxLen; i++){
            for(int j = 0; j <= targetLen; j++){
                scores[i][j] = 0;
                step[i][j][0] = step[i][j][1] = step[i][j][2] = 0;
            }
        }
        queryPos = -1;
        queryRes = targetRes = null;
    }
    public void align(String query, String target) {
        reset();
        len1 = query.length();
        len2 = target.length();
        byte[] s1 = query.getBytes();
        byte[] s2 = target.getBytes();
        int misMatch, leftGap, rightGap;//record the best point
        queryRes = targetRes = null;
        for (int index = 0; index < len1; index++) {
            for (int index2 = 0; index2 < len2; index2++) {
                if (s1[index] == s2[index2]) {
                    scores[index + 1][index2 + 1] = scores[index][index2] + match;
                    step[index + 1][index2 + 1][0] = 1;
                    step[index + 1][index2 + 1][1] = step[index][index2][1];
                    step[index + 1][index2 + 1][2] = step[index][index2][2];
                } else {
                    misMatch = scores[index][index2] + mismatch;
                    leftGap = scores[index][index2 + 1] + (step[index][index2 + 1][0] >= 3 ? gapExt : gap);
                    rightGap = scores[index + 1][index2] + (step[index + 1][index2][0] >= 3 ? gapExt : gap);
                    scores[index + 1][index2 + 1] = Math.max(misMatch, Math.max(leftGap, rightGap));
                    if (scores[index + 1][index2 + 1] == misMatch) {
                        //还在mismatch数量的允许范围内
                        if (step[index][index2][1] + 1 <= allMismatch) {
                            step[index + 1][index2 + 1][0] = 2;
                            step[index + 1][index2 + 1][1] = step[index][index2][1] + 1;
                            step[index + 1][index2 + 1][2] = step[index][index2][2];
                        } else
                            scores[index + 1][index2 + 1] = 0;

                    } else {
                        if (scores[index + 1][index2 + 1] == leftGap) {
                            if (step[index][index2 + 1][2] + 1 <= allGap) {
                                step[index + 1][index2 + 1][0] = 3;
                                step[index + 1][index2 + 1][1] = step[index][index2 + 1][1];
                                step[index + 1][index2 + 1][2] = step[index][index2 + 1][2] + 1;
                            } else
                                scores[index + 1][index2 + 1] = 0;

                        } else {
                            if (step[index + 1][index2][2] + 1 <= allGap) {
                                step[index + 1][index2 + 1][0] = 4;
                                step[index + 1][index2 + 1][1] = step[index + 1][index2][1];
                                step[index + 1][index2 + 1][2] = step[index + 1][index2][2] + 1;
//                                scores[index + 1][index2 + 1] = scores[index + 1][index2];//query skip not important
                            } else
                                scores[index + 1][index2 + 1] = 0;
                        }
                    }
                    if (scores[index + 1][index2 + 1] < -2) {
                        scores[index + 1][index2 + 1] = 0;
                    }
                }

            }
        }

        int needScore = match * len2 + allMismatch * mismatch + allGap * gap;
        int recordIndex = 0, maxScore = -100;
        for (int index = 0; index <= len1; index++) {
            if (scores[index][len2] >= needScore) {
                recordIndex = index;
                break;
            } else {
                if (scores[index][len2] > maxScore) {
                    maxScore = scores[index][len2];
                    recordIndex = index;
                }
            }
        }
        if(maxScore < needScore)
            queryPos = -1;
        int x = recordIndex, y = len2;
        StringBuilder align1 = new StringBuilder(), align2 = new StringBuilder();
        while (step[x][y][0] != 0) {
            if (step[x][y][0] == 3) {
                x--;
                align1.append((char) s1[x]);
                align2.append('-');

            } else if (step[x][y][0] == 4) {
                y--;
                align1.append('-');
                align2.append((char) s2[y]);

            } else {
                x--;
                y--;
                align1.append((char) s1[x]);
                align2.append((char) s2[y]);
            }
        }

        queryRes = align1.reverse().toString();
        targetRes = align2.reverse().toString();
        queryPos = x;

    }
}
