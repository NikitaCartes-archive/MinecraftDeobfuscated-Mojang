package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
	private final List<Fluid> fluids;
	public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(
		instance -> stateTestingCodec(instance)
				.and(Registry.FLUID.listOf().fieldOf("fluids").forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.fluids))
				.apply(instance, MatchingFluidsPredicate::new)
	);

	public MatchingFluidsPredicate(BlockPos blockPos, List<Fluid> list) {
		super(blockPos);
		this.fluids = list;
	}

	@Override
	protected boolean test(BlockState blockState) {
		return this.fluids.contains(blockState.getFluidState().getType());
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.MATCHING_FLUIDS;
	}
}
