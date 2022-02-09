/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class TagMatchTest
extends RuleTest {
    public static final Codec<TagMatchTest> CODEC = ((MapCodec)TagKey.codec(Registry.BLOCK_REGISTRY).fieldOf("tag")).xmap(TagMatchTest::new, tagMatchTest -> tagMatchTest.tag).codec();
    private final TagKey<Block> tag;

    public TagMatchTest(TagKey<Block> tagKey) {
        this.tag = tagKey;
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.tag);
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.TAG_TEST;
    }
}

