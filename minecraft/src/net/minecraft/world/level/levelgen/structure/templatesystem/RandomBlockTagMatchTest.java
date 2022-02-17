package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockTagMatchTest extends RuleTest {
	public static final Codec<RandomBlockTagMatchTest> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("block_tag").forGetter(randomBlockTagMatchTest -> randomBlockTagMatchTest.blockTag),
					Codec.FLOAT.fieldOf("probability").forGetter(randomBlockTagMatchTest -> randomBlockTagMatchTest.probability)
				)
				.apply(instance, RandomBlockTagMatchTest::new)
	);
	private final ResourceLocation blockTag;
	private final float probability;

	public RandomBlockTagMatchTest(Tag.Named<Block> named, float f) {
		this(named.getName(), f);
	}

	public RandomBlockTagMatchTest(ResourceLocation resourceLocation, float f) {
		this.blockTag = resourceLocation;
		this.probability = f;
	}

	@Override
	public boolean test(BlockState blockState, Random random) {
		return this.getBlockTagPredicate().test(blockState) && random.nextFloat() < this.probability;
	}

	@Override
	protected RuleTestType<?> getType() {
		return RuleTestType.RANDOM_BLOCK_TAG_TEST;
	}

	private Predicate<BlockState> getBlockTagPredicate() {
		Tag<Block> tag = BlockTags.getAllTags().getTag(this.blockTag);
		return tag == null ? blockState -> false : blockState -> blockState.is(tag);
	}
}
