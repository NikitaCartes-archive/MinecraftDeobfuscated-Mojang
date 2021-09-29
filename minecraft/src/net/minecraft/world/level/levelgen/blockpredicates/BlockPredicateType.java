package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface BlockPredicateType<P extends BlockPredicate> {
	BlockPredicateType<MatchingBlocksPredicate> MATCHING_BLOCKS = register("matching_blocks", MatchingBlocksPredicate.CODEC);
	BlockPredicateType<MatchingFluidsPredicate> MATCHING_FLUIDS = register("matching_fluids", MatchingFluidsPredicate.CODEC);
	BlockPredicateType<ReplaceablePredicate> REPLACEABLE = register("replaceable", ReplaceablePredicate.CODEC);
	BlockPredicateType<AnyOfPredicate> ANY_OF = register("any_of", AnyOfPredicate.CODEC);
	BlockPredicateType<AllOfPredicate> ALL_OF = register("all_of", AllOfPredicate.CODEC);
	BlockPredicateType<NotPredicate> NOT = register("not", NotPredicate.CODEC);

	Codec<P> codec();

	private static <P extends BlockPredicate> BlockPredicateType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.BLOCK_PREDICATE_TYPES, string, () -> codec);
	}
}
