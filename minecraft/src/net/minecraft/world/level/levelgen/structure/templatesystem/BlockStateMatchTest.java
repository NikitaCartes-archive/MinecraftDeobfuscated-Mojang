package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
	public static final Codec<BlockStateMatchTest> CODEC = BlockState.CODEC
		.fieldOf("block_state")
		.<BlockStateMatchTest>xmap(BlockStateMatchTest::new, blockStateMatchTest -> blockStateMatchTest.blockState)
		.codec();
	private final BlockState blockState;

	public BlockStateMatchTest(BlockState blockState) {
		this.blockState = blockState;
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return blockState == this.blockState;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.BLOCKSTATE_TEST;
	}
}
