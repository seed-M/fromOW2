package org.sat4j.pb.constraints.pb;

import java.math.BigInteger;

import org.sat4j.pb.core.PBSolverStats;

public class ConflictMapReduceByPowersOf2 extends ConflictMap {

    public ConflictMapReduceByPowersOf2(PBConstr cpb, int level) {
        super(cpb, level);
        // TODO Auto-generated constructor stub
    }

    public ConflictMapReduceByPowersOf2(PBConstr cpb, int level,
            boolean noRemove) {
        super(cpb, level, noRemove);
        // TODO Auto-generated constructor stub
    }

    public ConflictMapReduceByPowersOf2(PBConstr cpb, int level,
            boolean noRemove, IPostProcess postProcessing,
            PBSolverStats stats) {
        super(cpb, level, noRemove, postProcessing, stats);
        // TODO Auto-generated constructor stub
    }

    public static IConflict createConflict(PBConstr cpb, int level,
            PBSolverStats stats) {
        return new ConflictMapReduceByPowersOf2(cpb, level, true,
                NoPostProcess.instance(), stats);
    }

    public static IConflict createConflict(PBConstr cpb, int level,
            boolean noRemove, PBSolverStats stats) {
        return new ConflictMapReduceByPowersOf2(cpb, level, noRemove,
                NoPostProcess.instance(), stats);
    }

    @Override
    void decreaseCoefs() {
        String origine = this.toString();
        BigInteger previousSlack = this.currentSlack;
        int nbBits = reduceCoeffsByPower2();
        if (nbBits > 0) {
            stats.numberOfReductionsByPower2++;
            stats.numberOfRightShiftsForCoeffs = stats.numberOfRightShiftsForCoeffs
                    + nbBits;
        }
    }

}
