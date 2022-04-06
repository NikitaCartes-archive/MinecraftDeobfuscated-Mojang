package net.minecraft.world.level.levelgen.placement;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class RepeatingPlacement extends PlacementModifier {
	protected abstract int count(RandomSource randomSource, BlockPos blockPos);

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		return IntStream.range(0, this.count(randomSource, blockPos)).mapToObj(i -> blockPos);
	}
}
