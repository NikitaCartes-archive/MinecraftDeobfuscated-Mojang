/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockStateMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public interface RuleTestType
extends Deserializer<RuleTest> {
    public static final RuleTestType ALWAYS_TRUE_TEST = RuleTestType.register("always_true", dynamic -> AlwaysTrueTest.INSTANCE);
    public static final RuleTestType BLOCK_TEST = RuleTestType.register("block_match", BlockMatchTest::new);
    public static final RuleTestType BLOCKSTATE_TEST = RuleTestType.register("blockstate_match", BlockStateMatchTest::new);
    public static final RuleTestType TAG_TEST = RuleTestType.register("tag_match", TagMatchTest::new);
    public static final RuleTestType RANDOM_BLOCK_TEST = RuleTestType.register("random_block_match", RandomBlockMatchTest::new);
    public static final RuleTestType RANDOM_BLOCKSTATE_TEST = RuleTestType.register("random_blockstate_match", RandomBlockStateMatchTest::new);

    public static RuleTestType register(String string, RuleTestType ruleTestType) {
        return Registry.register(Registry.RULE_TEST, string, ruleTestType);
    }
}

