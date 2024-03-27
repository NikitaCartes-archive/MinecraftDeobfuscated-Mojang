package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
	public static final MapCodec<BlockStateMatchTest> CODEC = BlockState.CODEC
		.fieldOf("block_state")
		.xmap(BlockStateMatchTest::new, blockStateMatchTest -> blockStateMatchTest.blockState);
	private final BlockState blockState;

	public BlockStateMatchTest(BlockState blockState) {
		this.blockState = blockState;
	}

	@Override
	public boolean test(BlockState blockState, RandomSource randomSource) {
		return blockState == this.blockState;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.BLOCKSTATE_TEST;
	}
}
