/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BlockStateConfiguration
implements FeatureConfiguration {
    public static final Codec<BlockStateConfiguration> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(BlockStateConfiguration::new, blockStateConfiguration -> blockStateConfiguration.state).codec();
    public final BlockState state;

    public BlockStateConfiguration(BlockState blockState) {
        this.state = blockState;
    }
}

