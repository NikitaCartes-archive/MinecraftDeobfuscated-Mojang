/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
    private final List<PointCharge> charges = Lists.newArrayList();

    public void addCharge(BlockPos blockPos, double d) {
        if (d != 0.0) {
            this.charges.add(new PointCharge(blockPos, d));
        }
    }

    public double getPotentialEnergyChange(BlockPos blockPos, double d) {
        if (d == 0.0) {
            return 0.0;
        }
        double e = 0.0;
        for (PointCharge pointCharge : this.charges) {
            e += pointCharge.getPotentialChange(blockPos);
        }
        return e * d;
    }

    static class PointCharge {
        private final BlockPos pos;
        private final double charge;

        public PointCharge(BlockPos blockPos, double d) {
            this.pos = blockPos;
            this.charge = d;
        }

        public double getPotentialChange(BlockPos blockPos) {
            double d = this.pos.distSqr(blockPos);
            if (d == 0.0) {
                return Double.POSITIVE_INFINITY;
            }
            return this.charge / Math.sqrt(d);
        }
    }
}

