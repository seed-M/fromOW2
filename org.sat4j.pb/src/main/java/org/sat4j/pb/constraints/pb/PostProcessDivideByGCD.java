package org.sat4j.pb.constraints.pb;

public class PostProcessDivideByGCD implements IPostProcess {

    private static final PostProcessDivideByGCD INSTANCE = new PostProcessDivideByGCD();

    private PostProcessDivideByGCD() {
        // no instantiation
    }

    public static final PostProcessDivideByGCD instance() {
        return INSTANCE;
    }

    public void postProcess(int dl, ConflictMap conflictMap) {
        int gcd = conflictMap.reduceCoeffsByGCD();
        if (gcd > 1) {
            conflictMap.stats.numberOfReductionsByGCD++;
        }

    }

}
