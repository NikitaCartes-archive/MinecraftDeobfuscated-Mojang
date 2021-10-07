package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

class ReplaceablePredicate extends StateTestingPredicate {
	public static final Codec<ReplaceablePredicate> CODEC = RecordCodecBuilder.create(
		instance -> stateTestingCodec(instance).apply(instance, ReplaceablePredicate::new)
	);

	public ReplaceablePredicate(BlockPos blockPos) {
		super(blockPos);
	}

	@Override
	protected boolean test(BlockState blockState) {
		return blockState.getMaterial().isReplaceable();
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.REPLACEABLE;
	}
}
