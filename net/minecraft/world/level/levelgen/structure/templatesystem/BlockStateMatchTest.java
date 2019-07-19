/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class BlockStateMatchTest
extends RuleTest {
    private final BlockState blockState;

    public BlockStateMatchTest(BlockState blockState) {
        this.blockState = blockState;
    }

    public <T> BlockStateMatchTest(Dynamic<T> dynamic) {
        this(BlockState.deserialize(dynamic.get("blockstate").orElseEmptyMap()));
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState == this.blockState;
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.BLOCKSTATE_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("blockstate"), BlockState.serialize(dynamicOps, this.blockState).getValue())));
    }
}

