package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class SurfaceRelativeThresholdDecorator extends FeatureDecorator<SurfaceRelativeThresholdConfiguration> {
	public SurfaceRelativeThresholdDecorator(Codec<SurfaceRelativeThresholdConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		DecorationContext decorationContext, Random random, SurfaceRelativeThresholdConfiguration surfaceRelativeThresholdConfiguration, BlockPos blockPos
	) {
		long l = (long)decorationContext.getHeight(surfaceRelativeThresholdConfiguration.heightmap, blockPos.getX(), blockPos.getZ());
		long m = l + (long)surfaceRelativeThresholdConfiguration.minInclusive;
		long n = l + (long)surfaceRelativeThresholdConfiguration.maxInclusive;
		return (long)blockPos.getY() >= m && (long)blockPos.getY() <= n ? Stream.of(blockPos) : Stream.of();
	}
}
