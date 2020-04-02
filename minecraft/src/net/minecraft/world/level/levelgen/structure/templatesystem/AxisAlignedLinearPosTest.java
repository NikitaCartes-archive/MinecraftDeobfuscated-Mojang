package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class AxisAlignedLinearPosTest extends PosRuleTest {
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

	public <T> AxisAlignedLinearPosTest(Dynamic<T> dynamic) {
		this(
			dynamic.get("min_chance").asFloat(0.0F),
			dynamic.get("max_chance").asFloat(0.0F),
			dynamic.get("min_dist").asInt(0),
			dynamic.get("max_dist").asInt(0),
			Direction.Axis.byName(dynamic.get("axis").asString("y"))
		);
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
		float f = (float)Math.abs((blockPos2.getX() - blockPos3.getX()) * direction.getStepX());
		float g = (float)Math.abs((blockPos2.getY() - blockPos3.getY()) * direction.getStepY());
		float h = (float)Math.abs((blockPos2.getZ() - blockPos3.getZ()) * direction.getStepZ());
		int i = (int)(f + g + h);
		float j = random.nextFloat();
		return (double)j <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.inverseLerp((double)i, (double)this.minDist, (double)this.maxDist));
	}

	@Override
	protected PosRuleTestType getType() {
		return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("min_chance"),
					dynamicOps.createFloat(this.minChance),
					dynamicOps.createString("max_chance"),
					dynamicOps.createFloat(this.maxChance),
					dynamicOps.createString("min_dist"),
					dynamicOps.createFloat((float)this.minDist),
					dynamicOps.createString("max_dist"),
					dynamicOps.createFloat((float)this.maxDist),
					dynamicOps.createString("axis"),
					dynamicOps.createString(this.axis.getName())
				)
			)
		);
	}
}
