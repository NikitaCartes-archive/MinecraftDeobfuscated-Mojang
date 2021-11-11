package net.minecraft.world.level.levelgen.placement;

import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public abstract class PlacementFilter extends PlacementModifier {
	@Override
	public final Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
		return this.shouldPlace(placementContext, random, blockPos) ? Stream.of(blockPos) : Stream.of();
	}

	protected abstract boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos);
}
