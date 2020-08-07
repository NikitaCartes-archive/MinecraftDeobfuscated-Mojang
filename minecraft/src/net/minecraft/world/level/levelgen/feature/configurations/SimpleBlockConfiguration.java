package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockConfiguration implements FeatureConfiguration {
	public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("to_place").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.toPlace),
					BlockState.CODEC.listOf().fieldOf("place_on").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeOn),
					BlockState.CODEC.listOf().fieldOf("place_in").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeIn),
					BlockState.CODEC.listOf().fieldOf("place_under").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.placeUnder)
				)
				.apply(instance, SimpleBlockConfiguration::new)
	);
	public final BlockState toPlace;
	public final List<BlockState> placeOn;
	public final List<BlockState> placeIn;
	public final List<BlockState> placeUnder;

	public SimpleBlockConfiguration(BlockState blockState, List<BlockState> list, List<BlockState> list2, List<BlockState> list3) {
		this.toPlace = blockState;
		this.placeOn = list;
		this.placeIn = list2;
		this.placeUnder = list3;
	}
}
