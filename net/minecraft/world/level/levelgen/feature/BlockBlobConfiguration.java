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

public class BlockBlobConfiguration
implements FeatureConfiguration {
    public final BlockState state;
    public final int startRadius;

    public BlockBlobConfiguration(BlockState blockState, int i) {
        this.state = blockState;
        this.startRadius = i;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.state).getValue(), dynamicOps.createString("start_radius"), dynamicOps.createInt(this.startRadius))));
    }

    public static <T> BlockBlobConfiguration deserialize(Dynamic<T> dynamic) {
        BlockState blockState = dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int i = dynamic.get("start_radius").asInt(0);
        return new BlockBlobConfiguration(blockState, i);
    }
}

