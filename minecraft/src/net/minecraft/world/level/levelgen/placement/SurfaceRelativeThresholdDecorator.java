package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class SurfaceRelativeThresholdDecorator extends FilterDecorator<SurfaceRelativeThresholdConfiguration> {
	public SurfaceRelativeThresholdDecorator(Codec<SurfaceRelativeThresholdConfiguration> codec) {
		super(codec);
	}

	protected boolean shouldPlace(
		DecorationContext decorationContext, Random random, SurfaceRelativeThresholdConfiguration surfaceRelativeThresholdConfiguration, BlockPos blockPos
	) {
		long l = (long)decorationContext.getHeight(surfaceRelativeThresholdConfiguration.heightmap, blockPos.getX(), blockPos.getZ());
		long m = l + (long)surfaceRelativeThresholdConfiguration.minInclusive;
		long n = l + (long)surfaceRelativeThresholdConfiguration.maxInclusive;
		return m <= (long)blockPos.getY() && (long)blockPos.getY() <= n;
	}
}
