package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("contents").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.contents),
					BlockState.CODEC.fieldOf("rim").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.rim),
					UniformInt.codec(0, 8, 8).fieldOf("size").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.size),
					UniformInt.codec(0, 8, 8).fieldOf("rim_size").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.rimSize)
				)
				.apply(instance, DeltaFeatureConfiguration::new)
	);
	private final BlockState contents;
	private final BlockState rim;
	private final UniformInt size;
	private final UniformInt rimSize;

	public DeltaFeatureConfiguration(BlockState blockState, BlockState blockState2, UniformInt uniformInt, UniformInt uniformInt2) {
		this.contents = blockState;
		this.rim = blockState2;
		this.size = uniformInt;
		this.rimSize = uniformInt2;
	}

	public BlockState contents() {
		return this.contents;
	}

	public BlockState rim() {
		return this.rim;
	}

	public UniformInt size() {
		return this.size;
	}

	public UniformInt rimSize() {
		return this.rimSize;
	}
}
