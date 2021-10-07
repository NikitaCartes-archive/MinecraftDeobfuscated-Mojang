/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate
extends StateTestingPredicate {
    private final List<Fluid> fluids;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingFluidsPredicate.stateTestingCodec(instance).and(((MapCodec)Registry.FLUID.listOf().fieldOf("fluids")).forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.fluids)).apply((Applicative<MatchingFluidsPredicate, ?>)instance, MatchingFluidsPredicate::new));

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

