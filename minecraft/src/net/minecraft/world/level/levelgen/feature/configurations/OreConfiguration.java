package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class OreConfiguration implements FeatureConfiguration {
	public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(oreConfiguration -> oreConfiguration.targetStates),
					Codec.intRange(0, 64).fieldOf("size").forGetter(oreConfiguration -> oreConfiguration.size),
					Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(oreConfiguration -> oreConfiguration.discardChanceOnAirExposure)
				)
				.apply(instance, OreConfiguration::new)
	);
	public final List<OreConfiguration.TargetBlockState> targetStates;
	public final int size;
	public final float discardChanceOnAirExposure;

	public OreConfiguration(List<OreConfiguration.TargetBlockState> list, int i, float f) {
		this.size = i;
		this.targetStates = list;
		this.discardChanceOnAirExposure = f;
	}

	public OreConfiguration(List<OreConfiguration.TargetBlockState> list, int i) {
		this(list, i, 0.0F);
	}

	public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i, float f) {
		this(ImmutableList.of(new OreConfiguration.TargetBlockState(ruleTest, blockState)), i, f);
	}

	public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i) {
		this(ImmutableList.of(new OreConfiguration.TargetBlockState(ruleTest, blockState)), i, 0.0F);
	}

	public static OreConfiguration.TargetBlockState target(RuleTest ruleTest, BlockState blockState) {
		return new OreConfiguration.TargetBlockState(ruleTest, blockState);
	}

	public static final class Predicates {
		public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
		public static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
		public static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
		public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
		public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
	}

	public static class TargetBlockState {
		public static final Codec<OreConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RuleTest.CODEC.fieldOf("target").forGetter(targetBlockState -> targetBlockState.target),
						BlockState.CODEC.fieldOf("state").forGetter(targetBlockState -> targetBlockState.state)
					)
					.apply(instance, OreConfiguration.TargetBlockState::new)
		);
		public final RuleTest target;
		public final BlockState state;

		TargetBlockState(RuleTest ruleTest, BlockState blockState) {
			this.target = ruleTest;
			this.state = blockState;
		}
	}
}
