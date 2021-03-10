package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class SimpleBlockConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockStateProvider.CODEC.fieldOf("to_place").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.toPlace),
					BlockState.CODEC.listOf().fieldOf("place_on").orElse(ImmutableList.of()).forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeOn),
					BlockState.CODEC.listOf().fieldOf("place_in").orElse(ImmutableList.of()).forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeIn),
					BlockState.CODEC.listOf().fieldOf("place_under").orElse(ImmutableList.of()).forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeUnder)
				)
				.apply(instance, SimpleBlockConfiguration::new)
	);
	public final BlockStateProvider toPlace;
	public final List<BlockState> placeOn;
	public final List<BlockState> placeIn;
	public final List<BlockState> placeUnder;

	public SimpleBlockConfiguration(BlockStateProvider blockStateProvider, List<BlockState> list, List<BlockState> list2, List<BlockState> list3) {
		this.toPlace = blockStateProvider;
		this.placeOn = list;
		this.placeIn = list2;
		this.placeUnder = list3;
	}

	public SimpleBlockConfiguration(BlockStateProvider blockStateProvider) {
		this(blockStateProvider, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
	}
}
