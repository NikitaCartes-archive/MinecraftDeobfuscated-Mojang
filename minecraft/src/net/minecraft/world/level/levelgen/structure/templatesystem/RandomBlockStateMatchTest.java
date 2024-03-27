package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockStateMatchTest extends RuleTest {
	public static final MapCodec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("block_state").forGetter(randomBlockStateMatchTest -> randomBlockStateMatchTest.blockState),
					Codec.FLOAT.fieldOf("probability").forGetter(randomBlockStateMatchTest -> randomBlockStateMatchTest.probability)
				)
				.apply(instance, RandomBlockStateMatchTest::new)
	);
	private final BlockState blockState;
	private final float probability;

	public RandomBlockStateMatchTest(BlockState blockState, float f) {
		this.blockState = blockState;
		this.probability = f;
	}

	@Override
	public boolean test(BlockState blockState, RandomSource randomSource) {
		return blockState == this.blockState && randomSource.nextFloat() < this.probability;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.RANDOM_BLOCKSTATE_TEST;
	}
}
