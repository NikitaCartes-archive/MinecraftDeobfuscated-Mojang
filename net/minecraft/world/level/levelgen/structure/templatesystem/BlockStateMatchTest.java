/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class BlockStateMatchTest
extends RuleTest {
    public static final Codec<BlockStateMatchTest> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("block_state")).xmap(BlockStateMatchTest::new, blockStateMatchTest -> blockStateMatchTest.blockState).codec();
    private final BlockState blockState;

    public BlockStateMatchTest(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState == this.blockState;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.BLOCKSTATE_TEST;
    }
}

