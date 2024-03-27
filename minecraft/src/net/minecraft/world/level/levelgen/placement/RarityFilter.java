package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public class RarityFilter extends PlacementFilter {
	public static final MapCodec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, rarityFilter -> rarityFilter.chance);
	private final int chance;

	private RarityFilter(int i) {
		this.chance = i;
	}

	public static RarityFilter onAverageOnceEvery(int i) {
		return new RarityFilter(i);
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		return randomSource.nextFloat() < 1.0F / (float)this.chance;
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.RARITY_FILTER;
	}
}
