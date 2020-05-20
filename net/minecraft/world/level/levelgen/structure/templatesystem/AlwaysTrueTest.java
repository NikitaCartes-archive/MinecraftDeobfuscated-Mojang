/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class AlwaysTrueTest
extends RuleTest {
    public static final Codec<AlwaysTrueTest> CODEC = Codec.unit(() -> INSTANCE);
    public static final AlwaysTrueTest INSTANCE = new AlwaysTrueTest();

    private AlwaysTrueTest() {
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return true;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.ALWAYS_TRUE_TEST;
    }
}

