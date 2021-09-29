package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate implements BlockPredicate {
	private final List<Fluid> fluids;
	private final BlockPos offset;
	public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.FLUID.listOf().fieldOf("fluids").forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.fluids),
					BlockPos.CODEC.fieldOf("offset").forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.offset)
				)
				.apply(instance, MatchingFluidsPredicate::new)
	);

	public MatchingFluidsPredicate(List<Fluid> list, BlockPos blockPos) {
		this.fluids = list;
		this.offset = blockPos;
	}

	public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		return this.fluids.contains(worldGenLevel.getFluidState(blockPos.offset(this.offset)).getType());
	}

	@Override
	public BlockPredicateType<?> type() {
		return BlockPredicateType.MATCHING_FLUIDS;
	}
}
