package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class CloudPlacement extends PlacementModifier {
	public static final Codec<CloudPlacement> CODEC = Codec.unit(CloudPlacement::new);

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		int i = placementContext.getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ());
		return i != placementContext.getMinBuildHeight() ? Stream.empty() : Stream.of(blockPos.above(randomSource.nextIntBetweenInclusive(105, 115)));
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.CLOUD;
	}
}
