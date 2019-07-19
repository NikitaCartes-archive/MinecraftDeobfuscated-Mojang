/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;

public class LayerConfiguration
implements FeatureConfiguration {
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int i, BlockState blockState) {
        this.height = i;
        this.state = blockState;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("height"), dynamicOps.createInt(this.height), dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.state).getValue())));
    }

    public static <T> LayerConfiguration deserialize(Dynamic<T> dynamic) {
        int i = dynamic.get("height").asInt(0);
        BlockState blockState = dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        return new LayerConfiguration(i, blockState);
    }
}

