package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class AxisAlignedLinearPosTest extends PosRuleTest {
	public static final MapCodec<AxisAlignedLinearPosTest> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter(axisAlignedLinearPosTest -> axisAlignedLinearPosTest.minChance),
					Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter(axisAlignedLinearPosTest -> axisAlignedLinearPosTest.maxChance),
					Codec.INT.fieldOf("min_dist").orElse(0).forGetter(axisAlignedLinearPosTest -> axisAlignedLinearPosTest.minDist),
					Codec.INT.fieldOf("max_dist").orElse(0).forGetter(axisAlignedLinearPosTest -> axisAlignedLinearPosTest.maxDist),
					Direction.Axis.CODEC.fieldOf("axis").orElse(Direction.Axis.Y).forGetter(axisAlignedLinearPosTest -> axisAlignedLinearPosTest.axis)
				)
				.apply(instance, AxisAlignedLinearPosTest::new)
	);
	private final float minChance;
	private final float maxChance;
	private final int minDist;
	private final int maxDist;
	private final Direction.Axis axis;

	public AxisAlignedLinearPosTest(float f, float g, int i, int j, Direction.Axis axis) {
		if (i >= j) {
			throw new IllegalArgumentException("Invalid range: [" + i + "," + j + "]");
		} else {
			this.minChance = f;
			this.maxChance = g;
			this.minDist = i;
			this.maxDist = j;
			this.axis = axis;
		}
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, RandomSource randomSource) {
		Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
		float f = (float)Math.abs((blockPos2.getX() - blockPos3.getX()) * direction.getStepX());
		float g = (float)Math.abs((blockPos2.getY() - blockPos3.getY()) * direction.getStepY());
		float h = (float)Math.abs((blockPos2.getZ() - blockPos3.getZ()) * direction.getStepZ());
		int i = (int)(f + g + h);
		float j = randomSource.nextFloat();
		return j <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp((float)i, (float)this.minDist, (float)this.maxDist));
	}

	@Override
	protected PosRuleTestType<?> getType() {
		return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
	}
}
