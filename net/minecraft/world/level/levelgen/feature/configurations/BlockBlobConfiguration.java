/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BlockBlobConfiguration
implements FeatureConfiguration {
    public static final Codec<BlockBlobConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(blockBlobConfiguration -> blockBlobConfiguration.state), ((MapCodec)Codec.INT.fieldOf("start_radius")).withDefault(0).forGetter(blockBlobConfiguration -> blockBlobConfiguration.startRadius)).apply((Applicative<BlockBlobConfiguration, ?>)instance, BlockBlobConfiguration::new));
    public final BlockState state;
    public final int startRadius;

    public BlockBlobConfiguration(BlockState blockState, int i) {
        this.state = blockState;
        this.startRadius = i;
    }
}

