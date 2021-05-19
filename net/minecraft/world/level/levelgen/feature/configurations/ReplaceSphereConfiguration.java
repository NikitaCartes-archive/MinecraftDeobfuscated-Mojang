/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ReplaceSphereConfiguration
implements FeatureConfiguration {
    public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("target")).forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.targetState), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.replaceState), ((MapCodec)IntProvider.codec(0, 12).fieldOf("radius")).forGetter(replaceSphereConfiguration -> replaceSphereConfiguration.radius)).apply((Applicative<ReplaceSphereConfiguration, ?>)instance, ReplaceSphereConfiguration::new));
    public final BlockState targetState;
    public final BlockState replaceState;
    private final IntProvider radius;

    public ReplaceSphereConfiguration(BlockState blockState, BlockState blockState2, IntProvider intProvider) {
        this.targetState = blockState;
        this.replaceState = blockState2;
        this.radius = intProvider;
    }

    public IntProvider radius() {
        return this.radius;
    }
}

