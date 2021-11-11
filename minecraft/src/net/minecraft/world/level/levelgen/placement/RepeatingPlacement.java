package net.minecraft.world.level.levelgen.placement;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public abstract class RepeatingPlacement extends PlacementModifier {
	protected abstract int count(Random random, BlockPos blockPos);

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
		return IntStream.range(0, this.count(random, blockPos)).mapToObj(i -> blockPos);
	}
}
