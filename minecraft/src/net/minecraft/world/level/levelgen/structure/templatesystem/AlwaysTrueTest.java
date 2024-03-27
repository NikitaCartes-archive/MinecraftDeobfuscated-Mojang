package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class AlwaysTrueTest extends RuleTest {
	public static final MapCodec<AlwaysTrueTest> CODEC = MapCodec.unit((Supplier<AlwaysTrueTest>)(() -> AlwaysTrueTest.INSTANCE));
	public static final AlwaysTrueTest INSTANCE = new AlwaysTrueTest();

	private AlwaysTrueTest() {
	}

	@Override
	public boolean test(BlockState blockState, RandomSource randomSource) {
		return true;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.ALWAYS_TRUE_TEST;
	}
}
