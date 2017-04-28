package org.sat4j.pb.constraints.pb;

import org.sat4j.pb.core.PBSolverStats;

public class ConflictMapReduceByGCD extends ConflictMap {

    public ConflictMapReduceByGCD(PBConstr cpb, int level) {
        super(cpb, level);
        // TODO Auto-generated constructor stub
    }

    public ConflictMapReduceByGCD(PBConstr cpb, int level, boolean noRemove) {
        super(cpb, level, noRemove);
        // TODO Auto-generated constructor stub
    }

    public ConflictMapReduceByGCD(PBConstr cpb, int level, boolean noRemove,
            IPostProcess postProcessing, PBSolverStats stats) {
        super(cpb, level, noRemove, postProcessing, stats);
        // TODO Auto-generated constructor stub
    }

    public static IConflict createConflict(PBConstr cpb, int level,
            PBSolverStats stats) {
        return new ConflictMapReduceByGCD(cpb, level, true,
                NoPostProcess.instance(), stats);
    }

    public static IConflict createConflict(PBConstr cpb, int level,
            boolean noRemove, PBSolverStats stats) {
        return new ConflictMapReduceByGCD(cpb, level, noRemove,
                NoPostProcess.instance(), stats);
    }

    @Override
    void divideCoefs() {
        int gcd = reduceCoeffsByGCD();
        if (gcd > 1) {
            stats.numberOfReductionsByGCD++;
        }
    }

}
