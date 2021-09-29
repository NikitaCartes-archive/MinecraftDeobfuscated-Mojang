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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate
implements BlockPredicate {
    private final List<Fluid> fluids;
    private final BlockPos offset;
    public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.FLUID.listOf().fieldOf("fluids")).forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.fluids), ((MapCodec)BlockPos.CODEC.fieldOf("offset")).forGetter(matchingFluidsPredicate -> matchingFluidsPredicate.offset)).apply((Applicative<MatchingFluidsPredicate, ?>)instance, MatchingFluidsPredicate::new));

    public MatchingFluidsPredicate(List<Fluid> list, BlockPos blockPos) {
        this.fluids = list;
        this.offset = blockPos;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return this.fluids.contains(worldGenLevel.getFluidState(blockPos.offset(this.offset)).getType());
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_FLUIDS;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}

