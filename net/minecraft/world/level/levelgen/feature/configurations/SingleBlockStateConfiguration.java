/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public record SingleBlockStateConfiguration(BlockState state) implements DecoratorConfiguration
{
    public static final Codec<SingleBlockStateConfiguration> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("state")).xmap(SingleBlockStateConfiguration::new, SingleBlockStateConfiguration::state).codec();
}

