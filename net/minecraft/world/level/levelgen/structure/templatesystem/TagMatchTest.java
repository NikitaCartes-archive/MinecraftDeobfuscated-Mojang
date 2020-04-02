/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class TagMatchTest
extends RuleTest {
    private final Tag<Block> tag;

    public TagMatchTest(Tag<Block> tag) {
        this.tag = tag;
    }

    public <T> TagMatchTest(Dynamic<T> dynamic) {
        this(BlockTags.getAllTags().getTag(new ResourceLocation(dynamic.get("tag").asString(""))));
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.tag);
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.TAG_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("tag"), dynamicOps.createString(BlockTags.getAllTags().getIdOrThrow(this.tag).toString()))));
    }
}

