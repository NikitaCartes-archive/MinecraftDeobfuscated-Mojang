package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class PosAlwaysTrueTest extends PosRuleTest {
	public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

	private PosAlwaysTrueTest() {
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		return true;
	}

	@Override
	protected PosRuleTestType getType() {
		return PosRuleTestType.ALWAYS_TRUE_TEST;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.emptyMap());
	}
}
