package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;

public class AlwaysTrueTest extends RuleTest {
	public static final Codec<AlwaysTrueTest> CODEC = Codec.unit((Supplier<AlwaysTrueTest>)(() -> AlwaysTrueTest.INSTANCE));
	public static final AlwaysTrueTest INSTANCE = new AlwaysTrueTest();

	private AlwaysTrueTest() {
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return true;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.ALWAYS_TRUE_TEST;
	}
}
