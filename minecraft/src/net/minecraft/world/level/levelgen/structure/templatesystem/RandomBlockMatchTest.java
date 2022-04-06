package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
	public static final Codec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(randomBlockMatchTest -> randomBlockMatchTest.block),
					Codec.FLOAT.fieldOf("probability").forGetter(randomBlockMatchTest -> randomBlockMatchTest.probability)
				)
				.apply(instance, RandomBlockMatchTest::new)
	);
	private final Block block;
	private final float probability;

	public RandomBlockMatchTest(Block block, float f) {
		this.block = block;
		this.probability = f;
	}

	@Override
	public boolean test(BlockState blockState, RandomSource randomSource) {
		return blockState.is(this.block) && randomSource.nextFloat() < this.probability;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.RANDOM_BLOCK_TEST;
	}
}
