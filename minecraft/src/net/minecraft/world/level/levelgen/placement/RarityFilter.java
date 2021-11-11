package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;

public class RarityFilter extends PlacementFilter {
	public static final Codec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT
		.fieldOf("chance")
		.<RarityFilter>xmap(RarityFilter::new, rarityFilter -> rarityFilter.chance)
		.codec();
	private final int chance;

	private RarityFilter(int i) {
		this.chance = i;
	}

	public static RarityFilter onAverageOnceEvery(int i) {
		return new RarityFilter(i);
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, Random random, BlockPos blockPos) {
		return random.nextFloat() < 1.0F / (float)this.chance;
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.RARITY_FILTER;
	}
}
