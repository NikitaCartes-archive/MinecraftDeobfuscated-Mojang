package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceWaterDepthFilter extends PlacementFilter {
	public static final MapCodec<SurfaceWaterDepthFilter> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(Codec.INT.fieldOf("max_water_depth").forGetter(surfaceWaterDepthFilter -> surfaceWaterDepthFilter.maxWaterDepth))
				.apply(instance, SurfaceWaterDepthFilter::new)
	);
	private final int maxWaterDepth;

	private SurfaceWaterDepthFilter(int i) {
		this.maxWaterDepth = i;
	}

	public static SurfaceWaterDepthFilter forMaxDepth(int i) {
		return new SurfaceWaterDepthFilter(i);
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		int i = placementContext.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
		int j = placementContext.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ());
		return j - i <= this.maxWaterDepth;
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.SURFACE_WATER_DEPTH_FILTER;
	}
}
