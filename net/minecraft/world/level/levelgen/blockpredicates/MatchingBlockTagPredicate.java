/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

public class MatchingBlockTagPredicate
extends StateTestingPredicate {
    final Tag<Block> tag;
    public static final Codec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.create(instance -> MatchingBlockTagPredicate.stateTestingCodec(instance).and(((MapCodec)Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY)).fieldOf("tag")).forGetter(matchingBlockTagPredicate -> matchingBlockTagPredicate.tag)).apply((Applicative<MatchingBlockTagPredicate, ?>)instance, MatchingBlockTagPredicate::new));

    protected MatchingBlockTagPredicate(Vec3i vec3i, Tag<Block> tag) {
        super(vec3i);
        this.tag = tag;
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

