package net.minecraft.world.level.levelgen.placement;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class PlacementFilter extends PlacementModifier {
	@Override
	public final Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		return this.shouldPlace(placementContext, randomSource, blockPos) ? Stream.of(blockPos) : Stream.of();
	}

	protected abstract boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos);
}
