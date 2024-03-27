package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceRelativeThresholdFilter extends PlacementFilter {
	public static final MapCodec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(surfaceRelativeThresholdFilter -> surfaceRelativeThresholdFilter.heightmap),
					Codec.INT
						.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE))
						.forGetter(surfaceRelativeThresholdFilter -> surfaceRelativeThresholdFilter.minInclusive),
					Codec.INT
						.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE))
						.forGetter(surfaceRelativeThresholdFilter -> surfaceRelativeThresholdFilter.maxInclusive)
				)
				.apply(instance, SurfaceRelativeThresholdFilter::new)
	);
	private final Heightmap.Types heightmap;
	private final int minInclusive;
	private final int maxInclusive;

	private SurfaceRelativeThresholdFilter(Heightmap.Types types, int i, int j) {
		this.heightmap = types;
		this.minInclusive = i;
		this.maxInclusive = j;
	}

	public static SurfaceRelativeThresholdFilter of(Heightmap.Types types, int i, int j) {
		return new SurfaceRelativeThresholdFilter(types, i, j);
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		long l = (long)placementContext.getHeight(this.heightmap, blockPos.getX(), blockPos.getZ());
		long m = l + (long)this.minInclusive;
		long n = l + (long)this.maxInclusive;
		return m <= (long)blockPos.getY() && (long)blockPos.getY() <= n;
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
	}
}
