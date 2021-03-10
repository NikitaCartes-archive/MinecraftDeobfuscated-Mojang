package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class WaterDepthThresholdDecorator extends FeatureDecorator<WaterDepthThresholdConfiguration> {
	public WaterDepthThresholdDecorator(Codec<WaterDepthThresholdConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, WaterDepthThresholdConfiguration waterDepthThresholdConfiguration, BlockPos blockPos
	) {
		int i = decorationContext.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
		int j = decorationContext.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ());
		return j - i > waterDepthThresholdConfiguration.maxWaterDepth ? Stream.of() : Stream.of(blockPos);
	}
}
