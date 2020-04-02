package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class LinearPosTest extends PosRuleTest {
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

	public <T> LinearPosTest(Dynamic<T> dynamic) {
		this(dynamic.get("min_chance").asFloat(0.0F), dynamic.get("max_chance").asFloat(0.0F), dynamic.get("min_dist").asInt(0), dynamic.get("max_dist").asInt(0));
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		int i = blockPos2.distManhattan(blockPos3);
		float f = random.nextFloat();
		return (double)f <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.inverseLerp((double)i, (double)this.minDist, (double)this.maxDist));
	}

	@Override
	protected PosRuleTestType getType() {
		return PosRuleTestType.LINEAR_POS_TEST;
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
					dynamicOps.createFloat((float)this.maxDist)
				)
			)
		);
	}
}
