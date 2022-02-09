/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

class MatchingBlocksPredicate
extends StateTestingPredicate {
    private final HolderSet<Block> blocks;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingBlocksPredicate.stateTestingCodec(instance).and(((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("blocks")).forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks)).apply((Applicative<MatchingBlocksPredicate, ?>)instance, MatchingBlocksPredicate::new));

    public MatchingBlocksPredicate(Vec3i vec3i, HolderSet<Block> holderSet) {
        super(vec3i);
        this.blocks = holderSet;
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.is(this.blocks);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}

