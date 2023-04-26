package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
	public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("valid_base_block").forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.validBaseState),
					BlockState.CODEC.fieldOf("stem_state").forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.stemState),
					BlockState.CODEC.fieldOf("hat_state").forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.hatState),
					BlockState.CODEC.fieldOf("decor_state").forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.decorState),
					BlockPredicate.CODEC.fieldOf("replaceable_blocks").forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.replaceableBlocks),
					Codec.BOOL.fieldOf("planted").orElse(false).forGetter(hugeFungusConfiguration -> hugeFungusConfiguration.planted)
				)
				.apply(instance, HugeFungusConfiguration::new)
	);
	public final BlockState validBaseState;
	public final BlockState stemState;
	public final BlockState hatState;
	public final BlockState decorState;
	public final BlockPredicate replaceableBlocks;
	public final boolean planted;

	public HugeFungusConfiguration(
		BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, BlockPredicate blockPredicate, boolean bl
	) {
		this.validBaseState = blockState;
		this.stemState = blockState2;
		this.hatState = blockState3;
		this.decorState = blockState4;
		this.replaceableBlocks = blockPredicate;
		this.planted = bl;
	}
}
