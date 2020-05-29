package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;

public class PosAlwaysTrueTest extends PosRuleTest {
	public static final Codec<PosAlwaysTrueTest> CODEC = Codec.unit((Supplier<PosAlwaysTrueTest>)(() -> PosAlwaysTrueTest.INSTANCE));
	public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

	private PosAlwaysTrueTest() {
	}

	@Override
	public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		return true;
	}

	@Override
	protected PosRuleTestType<?> getType() {
		return PosRuleTestType.ALWAYS_TRUE_TEST;
	}
}