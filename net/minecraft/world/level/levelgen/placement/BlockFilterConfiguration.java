/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public record BlockFilterConfiguration(List<Block> allowed, List<Block> disallowed, BlockPos offset) implements DecoratorConfiguration
{
    public static final Codec<BlockFilterConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(Registry.BLOCK.listOf().optionalFieldOf("allowed", List.of()).forGetter(BlockFilterConfiguration::allowed), Registry.BLOCK.listOf().optionalFieldOf("disallowed", List.of()).forGetter(BlockFilterConfiguration::disallowed), ((MapCodec)BlockPos.CODEC.fieldOf("offset")).forGetter(BlockFilterConfiguration::offset)).apply((Applicative<BlockFilterConfiguration, ?>)instance, BlockFilterConfiguration::new));
}

