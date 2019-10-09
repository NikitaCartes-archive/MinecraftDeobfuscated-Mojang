/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;

public class DoublePlantPlacer
extends BlockPlacer {
    public DoublePlantPlacer() {
        super(BlockPlacerType.DOUBLE_PLANT_PLACER);
    }

    public <T> DoublePlantPlacer(Dynamic<T> dynamic) {
        this();
    }

    @Override
    public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
        ((DoublePlantBlock)blockState.getBlock()).placeAt(levelAccessor, blockPos, 2);
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCK_PLACER_TYPES.getKey(this.type).toString())))).getValue();
    }
}

