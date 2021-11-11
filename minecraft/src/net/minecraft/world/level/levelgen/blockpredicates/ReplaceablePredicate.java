package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

class ReplaceablePredicate extends StateTestingPredicate {
	public static final Codec<ReplaceablePredicate> CODEC = RecordCodecBuilder.create(
		instance -> stateTestingCodec(instance).apply(instance, ReplaceablePredicate::new)
	);

	public ReplaceablePredicate(Vec3i vec3i) {
		super(vec3i);
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
