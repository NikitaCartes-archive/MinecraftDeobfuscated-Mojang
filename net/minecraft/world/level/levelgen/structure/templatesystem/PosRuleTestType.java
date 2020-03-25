/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.AxisAlignedLinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LinearPosTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;

public interface PosRuleTestType
extends Deserializer<PosRuleTest> {
    public static final PosRuleTestType ALWAYS_TRUE_TEST = PosRuleTestType.register("always_true", dynamic -> PosAlwaysTrueTest.INSTANCE);
    public static final PosRuleTestType LINEAR_POS_TEST = PosRuleTestType.register("linear_pos", LinearPosTest::new);
    public static final PosRuleTestType AXIS_ALIGNED_LINEAR_POS_TEST = PosRuleTestType.register("axis_aligned_linear_pos", AxisAlignedLinearPosTest::new);

    public static PosRuleTestType register(String string, PosRuleTestType posRuleTestType) {
        return Registry.register(Registry.POS_RULE_TEST, string, posRuleTestType);
    }
}

