/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class RandomBlockMatchTest
extends RuleTest {
    public static final Codec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.BLOCK.byNameCodec().fieldOf("block")).forGetter(randomBlockMatchTest -> randomBlockMatchTest.block), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(randomBlockMatchTest -> Float.valueOf(randomBlockMatchTest.probability))).apply((Applicative<RandomBlockMatchTest, ?>)instance, RandomBlockMatchTest::new));
    private final Block block;
    private final float probability;

    public RandomBlockMatchTest(Block block, float f) {
        this.block = block;
        this.probability = f;
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.block) && random.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCK_TEST;
    }
}

