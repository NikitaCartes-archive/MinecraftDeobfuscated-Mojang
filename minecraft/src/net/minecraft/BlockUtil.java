package net.minecraft;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
	public static BlockUtil.FoundRectangle getLargestRectangleAround(
		BlockPos blockPos, Direction.Axis axis, int i, Direction.Axis axis2, int j, Predicate<BlockPos> predicate
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
		Direction direction2 = direction.getOpposite();
		Direction direction3 = Direction.get(Direction.AxisDirection.NEGATIVE, axis2);
		Direction direction4 = direction3.getOpposite();
		int k = getLimit(predicate, mutableBlockPos.set(blockPos), direction, i);
		int l = getLimit(predicate, mutableBlockPos.set(blockPos), direction2, i);
		int m = k;
		BlockUtil.IntBounds[] intBoundss = new BlockUtil.IntBounds[k + 1 + l];
		intBoundss[k] = new BlockUtil.IntBounds(
			getLimit(predicate, mutableBlockPos.set(blockPos), direction3, j), getLimit(predicate, mutableBlockPos.set(blockPos), direction4, j)
		);
		int n = intBoundss[k].min;

		for (int o = 1; o <= k; o++) {
			BlockUtil.IntBounds intBounds = intBoundss[m - (o - 1)];
			intBoundss[m - o] = new BlockUtil.IntBounds(
				getLimit(predicate, mutableBlockPos.set(blockPos).move(direction, o), direction3, intBounds.min),
				getLimit(predicate, mutableBlockPos.set(blockPos).move(direction, o), direction4, intBounds.max)
			);
		}

		for (int o = 1; o <= l; o++) {
			BlockUtil.IntBounds intBounds = intBoundss[m + o - 1];
			intBoundss[m + o] = new BlockUtil.IntBounds(
				getLimit(predicate, mutableBlockPos.set(blockPos).move(direction2, o), direction3, intBounds.min),
				getLimit(predicate, mutableBlockPos.set(blockPos).move(direction2, o), direction4, intBounds.max)
			);
		}

		int o = 0;
		int p = 0;
		int q = 0;
		int r = 0;
		int[] is = new int[intBoundss.length];

		for (int s = n; s >= 0; s--) {
			for (int t = 0; t < intBoundss.length; t++) {
				BlockUtil.IntBounds intBounds2 = intBoundss[t];
				int u = n - intBounds2.min;
				int v = n + intBounds2.max;
				is[t] = s >= u && s <= v ? v + 1 - s : 0;
			}

			Pair<BlockUtil.IntBounds, Integer> pair = getMaxRectangleLocation(is);
			BlockUtil.IntBounds intBounds2 = pair.getFirst();
			int u = 1 + intBounds2.max - intBounds2.min;
			int v = pair.getSecond();
			if (u * v > q * r) {
				o = intBounds2.min;
				p = s;
				q = u;
				r = v;
			}
		}

		return new BlockUtil.FoundRectangle(blockPos.relative(axis, o - m).relative(axis2, p - n), q, r);
	}

	private static int getLimit(Predicate<BlockPos> predicate, BlockPos.MutableBlockPos mutableBlockPos, Direction direction, int i) {
		int j = 0;

		while (j < i && predicate.test(mutableBlockPos.move(direction))) {
			j++;
		}

		return j;
	}

	@VisibleForTesting
	static Pair<BlockUtil.IntBounds, Integer> getMaxRectangleLocation(int[] is) {
		int i = 0;
		int j = 0;
		int k = 0;
		IntStack intStack = new IntArrayList();
		intStack.push(0);

		for (int l = 1; l <= is.length; l++) {
			int m = l == is.length ? 0 : is[l];

			while (!intStack.isEmpty()) {
				int n = is[intStack.topInt()];
				if (m >= n) {
					intStack.push(l);
					break;
				}

				intStack.popInt();
				int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
				if (n * (l - o) > k * (j - i)) {
					j = l;
					i = o;
					k = n;
				}
			}

			if (intStack.isEmpty()) {
				intStack.push(l);
			}
		}

		return new Pair<>(new BlockUtil.IntBounds(i, j - 1), k);
	}

	public static Optional<BlockPos> getTopConnectedBlock(BlockGetter blockGetter, BlockPos blockPos, Block block, Direction direction, Block block2) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		BlockState blockState;
		do {
			mutableBlockPos.move(direction);
			blockState = blockGetter.getBlockState(mutableBlockPos);
		} while (blockState.is(block));

		return blockState.is(block2) ? Optional.of(mutableBlockPos) : Optional.empty();
	}

	public static class FoundRectangle {
		public final BlockPos minCorner;
		public final int axis1Size;
		public final int axis2Size;

		public FoundRectangle(BlockPos blockPos, int i, int j) {
			this.minCorner = blockPos;
			this.axis1Size = i;
			this.axis2Size = j;
		}
	}

	public static class IntBounds {
		public final int min;
		public final int max;

		public IntBounds(int i, int j) {
			this.min = i;
			this.max = j;
		}

		public String toString() {
			return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
		}
	}
}
