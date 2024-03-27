package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class BiomeFilter extends PlacementFilter {
	private static final BiomeFilter INSTANCE = new BiomeFilter();
	public static MapCodec<BiomeFilter> CODEC = MapCodec.unit((Supplier<BiomeFilter>)(() -> INSTANCE));

	private BiomeFilter() {
	}

	public static BiomeFilter biome() {
		return INSTANCE;
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		PlacedFeature placedFeature = (PlacedFeature)placementContext.topFeature()
			.orElseThrow(() -> new IllegalStateException("Tried to biome check an unregistered feature, or a feature that should not restrict the biome"));
		Holder<Biome> holder = placementContext.getLevel().getBiome(blockPos);
		return placementContext.generator().getBiomeGenerationSettings(holder).hasFeature(placedFeature);
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.BIOME_FILTER;
	}
}
