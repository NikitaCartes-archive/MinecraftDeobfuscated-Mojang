/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record RuleBasedBlockStateProvider(BlockStateProvider fallback, List<Rule> rules) {
    public static final Codec<RuleBasedBlockStateProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("fallback")).forGetter(RuleBasedBlockStateProvider::fallback), ((MapCodec)Rule.CODEC.listOf().fieldOf("rules")).forGetter(RuleBasedBlockStateProvider::rules)).apply((Applicative<RuleBasedBlockStateProvider, ?>)instance, RuleBasedBlockStateProvider::new));

    public static RuleBasedBlockStateProvider simple(BlockStateProvider blockStateProvider) {
        return new RuleBasedBlockStateProvider(blockStateProvider, List.of());
    }

    public static RuleBasedBlockStateProvider simple(Block block) {
        return RuleBasedBlockStateProvider.simple(BlockStateProvider.simple(block));
    }

    public BlockState getState(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos) {
        for (Rule rule : this.rules) {
            if (!rule.ifTrue().test(worldGenLevel, blockPos)) continue;
            return rule.then().getState(randomSource, blockPos);
        }
        return this.fallback.getState(randomSource, blockPos);
    }

    public record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPredicate.CODEC.fieldOf("if_true")).forGetter(Rule::ifTrue), ((MapCodec)BlockStateProvider.CODEC.fieldOf("then")).forGetter(Rule::then)).apply((Applicative<Rule, ?>)instance, Rule::new));
    }
}

