package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
	private final List<PotentialCalculator.PointCharge> charges = Lists.<PotentialCalculator.PointCharge>newArrayList();

	public void addCharge(BlockPos blockPos, double d) {
		if (d != 0.0) {
			this.charges.add(new PotentialCalculator.PointCharge(blockPos, d));
		}
	}

	public double getPotentialEnergyChange(BlockPos blockPos, double d) {
		if (d == 0.0) {
			return 0.0;
		} else {
			double e = 0.0;

			for (PotentialCalculator.PointCharge pointCharge : this.charges) {
				e += pointCharge.getPotentialChange(blockPos);
			}

			return e * d;
		}
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
			return d == 0.0 ? Double.POSITIVE_INFINITY : this.charge / Math.sqrt(d);
		}
	}
}
