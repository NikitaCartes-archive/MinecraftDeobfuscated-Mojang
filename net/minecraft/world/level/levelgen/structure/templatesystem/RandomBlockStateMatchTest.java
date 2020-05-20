/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class RandomBlockStateMatchTest
extends RuleTest {
    public static final Codec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockState.CODEC.fieldOf("block_state")).forGetter(randomBlockStateMatchTest -> randomBlockStateMatchTest.blockState), ((MapCodec)Codec.FLOAT.fieldOf("probability")).forGetter(randomBlockStateMatchTest -> Float.valueOf(randomBlockStateMatchTest.probability))).apply((Applicative<RandomBlockStateMatchTest, ?>)instance, RandomBlockStateMatchTest::new));
    private final BlockState blockState;
    private final float probability;

    public RandomBlockStateMatchTest(BlockState blockState, float f) {
        this.blockState = blockState;
        this.probability = f;
    }

    @Override
    public boolean test(BlockState blockState, Random random) {
        return blockState == this.blockState && random.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCKSTATE_TEST;
    }
}

