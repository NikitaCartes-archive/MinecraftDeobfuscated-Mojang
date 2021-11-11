/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration
implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.list(TargetBlockState.CODEC).fieldOf("targets")).forGetter(oreConfiguration -> oreConfiguration.targetStates), ((MapCodec)Codec.intRange(0, 64).fieldOf("size")).forGetter(oreConfiguration -> oreConfiguration.size), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("discard_chance_on_air_exposure")).forGetter(oreConfiguration -> Float.valueOf(oreConfiguration.discardChanceOnAirExposure))).apply((Applicative<OreConfiguration, ?>)instance, OreConfiguration::new));
    public final List<TargetBlockState> targetStates;
    public final int size;
    public final float discardChanceOnAirExposure;

    public OreConfiguration(List<TargetBlockState> list, int i, float f) {
        this.size = i;
        this.targetStates = list;
        this.discardChanceOnAirExposure = f;
    }

    public OreConfiguration(List<TargetBlockState> list, int i) {
        this(list, i, 0.0f);
    }

    public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i, float f) {
        this(ImmutableList.of(new TargetBlockState(ruleTest, blockState)), i, f);
    }

    public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i) {
        this(ImmutableList.of(new TargetBlockState(ruleTest, blockState)), i, 0.0f);
    }

    public static TargetBlockState target(RuleTest ruleTest, BlockState blockState) {
        return new TargetBlockState(ruleTest, blockState);
    }

    public static class TargetBlockState {
        public static final Codec<TargetBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuleTest.CODEC.fieldOf("target")).forGetter(targetBlockState -> targetBlockState.target), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(targetBlockState -> targetBlockState.state)).apply((Applicative<TargetBlockState, ?>)instance, TargetBlockState::new));
        public final RuleTest target;
        public final BlockState state;

        TargetBlockState(RuleTest ruleTest, BlockState blockState) {
            this.target = ruleTest;
            this.state = blockState;
        }
    }
}

