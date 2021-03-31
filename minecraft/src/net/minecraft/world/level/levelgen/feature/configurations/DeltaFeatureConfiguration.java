package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("contents").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.contents),
					BlockState.CODEC.fieldOf("rim").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.rim),
					IntProvider.codec(0, 16).fieldOf("size").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.size),
					IntProvider.codec(0, 16).fieldOf("rim_size").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.rimSize)
				)
				.apply(instance, DeltaFeatureConfiguration::new)
	);
	private final BlockState contents;
	private final BlockState rim;
	private final IntProvider size;
	private final IntProvider rimSize;

	public DeltaFeatureConfiguration(BlockState blockState, BlockState blockState2, IntProvider intProvider, IntProvider intProvider2) {
		this.contents = blockState;
		this.rim = blockState2;
		this.size = intProvider;
		this.rimSize = intProvider2;
	}

	public BlockState contents() {
		return this.contents;
	}

	public BlockState rim() {
		return this.rim;
	}

	public IntProvider size() {
		return this.size;
	}

	public IntProvider rimSize() {
		return this.rimSize;
	}
}
