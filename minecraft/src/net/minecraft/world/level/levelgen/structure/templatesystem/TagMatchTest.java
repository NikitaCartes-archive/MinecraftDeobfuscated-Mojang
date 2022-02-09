package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
	public static final Codec<TagMatchTest> CODEC = TagKey.codec(Registry.BLOCK_REGISTRY)
		.fieldOf("tag")
		.<TagMatchTest>xmap(TagMatchTest::new, tagMatchTest -> tagMatchTest.tag)
		.codec();
	private final TagKey<Block> tag;

	public TagMatchTest(TagKey<Block> tagKey) {
		this.tag = tagKey;
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return blockState.is(this.tag);
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.TAG_TEST;
	}
}
