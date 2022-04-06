package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class BlockPredicateFilter extends PlacementFilter {
	public static final Codec<BlockPredicateFilter> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(blockPredicateFilter -> blockPredicateFilter.predicate))
				.apply(instance, BlockPredicateFilter::new)
	);
	private final BlockPredicate predicate;

	private BlockPredicateFilter(BlockPredicate blockPredicate) {
		this.predicate = blockPredicate;
	}

	public static BlockPredicateFilter forPredicate(BlockPredicate blockPredicate) {
		return new BlockPredicateFilter(blockPredicate);
	}

	@Override
	protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
		return this.predicate.test(placementContext.getLevel(), blockPos);
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.BLOCK_PREDICATE_FILTER;
	}
}
