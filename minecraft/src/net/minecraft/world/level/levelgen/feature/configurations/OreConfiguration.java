package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class OreConfiguration implements FeatureConfiguration {
	public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RuleTest.CODEC.fieldOf("target").forGetter(oreConfiguration -> oreConfiguration.target),
					BlockState.CODEC.fieldOf("state").forGetter(oreConfiguration -> oreConfiguration.state),
					Codec.intRange(0, 64).fieldOf("size").forGetter(oreConfiguration -> oreConfiguration.size)
				)
				.apply(instance, OreConfiguration::new)
	);
	public final RuleTest target;
	public final int size;
	public final BlockState state;

	public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i) {
		this.size = i;
		this.state = blockState;
		this.target = ruleTest;
	}

	public static final class Predicates {
		public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
		public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
		public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
	}
}
