/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

public class MatchingBlockTagPredicate
extends StateTestingPredicate {
    final TagKey<Block> tag;
    public static final Codec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingBlockTagPredicate.stateTestingCodec(instance).and(((MapCodec)TagKey.codec(Registries.BLOCK).fieldOf("tag")).forGetter(matchingBlockTagPredicate -> matchingBlockTagPredicate.tag)).apply((Applicative<MatchingBlockTagPredicate, ?>)instance, MatchingBlockTagPredicate::new));

    protected MatchingBlockTagPredicate(Vec3i vec3i, TagKey<Block> tagKey) {
        super(vec3i);
        this.tag = tagKey;
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.is(this.tag);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCK_TAG;
    }
}

