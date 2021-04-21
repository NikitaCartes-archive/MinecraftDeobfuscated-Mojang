package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
	public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.targetStates)
				)
				.apply(instance, ReplaceBlockConfiguration::new)
	);
	public final List<OreConfiguration.TargetBlockState> targetStates;

	public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
		this(ImmutableList.of(OreConfiguration.target(new BlockStateMatchTest(blockState), blockState2)));
	}

	public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> list) {
		this.targetStates = list;
	}
}
