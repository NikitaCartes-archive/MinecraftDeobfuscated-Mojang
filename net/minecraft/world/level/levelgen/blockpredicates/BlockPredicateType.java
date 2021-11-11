/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.blockpredicates.AllOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.AnyOfPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.InsideWorldBoundsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingBlocksPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.MatchingFluidsPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.NotPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.ReplaceablePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.SolidPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.TrueBlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate;

public interface BlockPredicateType<P extends BlockPredicate> {
    public static final BlockPredicateType<MatchingBlocksPredicate> MATCHING_BLOCKS = BlockPredicateType.register("matching_blocks", MatchingBlocksPredicate.CODEC);
    public static final BlockPredicateType<MatchingFluidsPredicate> MATCHING_FLUIDS = BlockPredicateType.register("matching_fluids", MatchingFluidsPredicate.CODEC);
    public static final BlockPredicateType<SolidPredicate> SOLID = BlockPredicateType.register("solid", SolidPredicate.CODEC);
    public static final BlockPredicateType<ReplaceablePredicate> REPLACEABLE = BlockPredicateType.register("replaceable", ReplaceablePredicate.CODEC);
    public static final BlockPredicateType<WouldSurvivePredicate> WOULD_SURVIVE = BlockPredicateType.register("would_survive", WouldSurvivePredicate.CODEC);
    public static final BlockPredicateType<InsideWorldBoundsPredicate> INSIDE_WORLD_BOUNDS = BlockPredicateType.register("inside_world_bounds", InsideWorldBoundsPredicate.CODEC);
    public static final BlockPredicateType<AnyOfPredicate> ANY_OF = BlockPredicateType.register("any_of", AnyOfPredicate.CODEC);
    public static final BlockPredicateType<AllOfPredicate> ALL_OF = BlockPredicateType.register("all_of", AllOfPredicate.CODEC);
    public static final BlockPredicateType<NotPredicate> NOT = BlockPredicateType.register("not", NotPredicate.CODEC);
    public static final BlockPredicateType<TrueBlockPredicate> TRUE = BlockPredicateType.register("true", TrueBlockPredicate.CODEC);

    public Codec<P> codec();

    private static <P extends BlockPredicate> BlockPredicateType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.BLOCK_PREDICATE_TYPES, string, () -> codec);
    }
}

