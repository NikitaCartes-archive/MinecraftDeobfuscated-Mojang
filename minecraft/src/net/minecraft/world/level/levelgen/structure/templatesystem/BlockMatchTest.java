package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
	public static final Codec<BlockMatchTest> CODEC = BuiltInRegistries.BLOCK
		.byNameCodec()
		.fieldOf("block")
		.<BlockMatchTest>xmap(BlockMatchTest::new, blockMatchTest -> blockMatchTest.block)
		.codec();
	private final Block block;

	public BlockMatchTest(Block block) {
		this.block = block;
	}

	@Override
	public boolean test(BlockState blockState, RandomSource randomSource) {
		return blockState.is(this.block);
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.BLOCK_TEST;
	}
}
