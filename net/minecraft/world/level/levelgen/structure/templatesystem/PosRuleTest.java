/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;

public abstract class PosRuleTest {
    public static final Codec<PosRuleTest> CODEC = Registry.POS_RULE_TEST.dispatch("predicate_type", PosRuleTest::getType, PosRuleTestType::codec);

    public abstract boolean test(BlockPos var1, BlockPos var2, BlockPos var3, Random var4);

    protected abstract PosRuleTestType<?> getType();
}

