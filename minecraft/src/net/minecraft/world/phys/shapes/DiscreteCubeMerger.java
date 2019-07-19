package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public final class DiscreteCubeMerger implements IndexMerger {
	private final CubePointRange result;
	private final int firstSize;
	private final int secondSize;
	private final int gcd;

	DiscreteCubeMerger(int i, int j) {
		this.result = new CubePointRange((int)Shapes.lcm(i, j));
		this.firstSize = i;
		this.secondSize = j;
		this.gcd = IntMath.gcd(i, j);
	}

	@Override
	public boolean forMergedIndexes(IndexMerger.IndexConsumer indexConsumer) {
		int i = this.firstSize / this.gcd;
		int j = this.secondSize / this.gcd;

		for (int k = 0; k <= this.result.size(); k++) {
			if (!indexConsumer.merge(k / j, k / i, k)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public DoubleList getList() {
		return this.result;
	}
}
