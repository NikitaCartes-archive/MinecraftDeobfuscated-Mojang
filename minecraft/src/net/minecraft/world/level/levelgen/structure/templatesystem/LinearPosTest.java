package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class LinearPosTest extends PosRuleTest {
	public static final Codec<LinearPosTest> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter(linearPosTest -> linearPosTest.minChance),
					Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter(linearPosTest -> linearPosTest.maxChance),
					Codec.INT.fieldOf("min_dist").orElse(0).forGetter(linearPosTest -> linearPosTest.minDist),
					Codec.INT.fieldOf("max_dist").orElse(0).forGetter(linearPosTest -> linearPosTest.maxDist)
				)
				.apply(instance, LinearPosTest::new)
	);
	private final float minChance;
	private final float maxChance;
	private final int minDist;
	private final int maxDist;

	public LinearPosTest(float f, float g, int i, int j) {
		if (i >= j) {
			throw new IllegalArgumentException("Invalid range: [" + i + "," + j + "]");
		} else {
			this.minChance = f;
			this.maxChance = g;
			this.minDist = i;
			this.maxDist = j;
		}
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		int i = blockPos2.distManhattan(blockPos3);
		float f = random.nextFloat();
		return (double)f <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.inverseLerp((double)i, (double)this.minDist, (double)this.maxDist));
	}

	@Override
	protected PosRuleTestType<?> getType() {
		return PosRuleTestType.LINEAR_POS_TEST;
	}
}
