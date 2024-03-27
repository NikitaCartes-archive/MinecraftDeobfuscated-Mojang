package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class PosAlwaysTrueTest extends PosRuleTest {
	public static final MapCodec<PosAlwaysTrueTest> CODEC = MapCodec.unit((Supplier<PosAlwaysTrueTest>)(() -> PosAlwaysTrueTest.INSTANCE));
	public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

	private PosAlwaysTrueTest() {
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, RandomSource randomSource) {
		return true;
	}

	@Override
	protected PosRuleTestType<?> getType() {
		return PosRuleTestType.ALWAYS_TRUE_TEST;
	}
}
