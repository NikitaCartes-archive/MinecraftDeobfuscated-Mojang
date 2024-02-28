package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class PathTypeCache {
	private static final int SIZE = 4096;
	private static final int MASK = 4095;
	private final long[] positions = new long[4096];
	private final PathType[] pathTypes = new PathType[4096];

	public PathType getOrCompute(BlockGetter blockGetter, BlockPos blockPos) {
		long l = blockPos.asLong();
		int i = index(l);
		PathType pathType = this.get(i, l);
		return pathType != null ? pathType : this.compute(blockGetter, blockPos, i, l);
	}

	@Nullable
	private PathType get(int i, long l) {
		return this.positions[i] == l ? this.pathTypes[i] : null;
	}

	private PathType compute(BlockGetter blockGetter, BlockPos blockPos, int i, long l) {
		PathType pathType = WalkNodeEvaluator.getPathTypeFromState(blockGetter, blockPos);
		this.positions[i] = l;
		this.pathTypes[i] = pathType;
		return pathType;
	}

	public void invalidate(BlockPos blockPos) {
		long l = blockPos.asLong();
		int i = index(l);
		if (this.positions[i] == l) {
			this.pathTypes[i] = null;
		}
	}

	private static int index(long l) {
		return (int)HashCommon.mix(l) & 4095;
	}
}
