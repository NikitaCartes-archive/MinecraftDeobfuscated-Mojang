package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("contents").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.contents),
					BlockState.CODEC.fieldOf("rim").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.rim),
					Codec.INT.fieldOf("minimum_radius").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.minimumRadius),
					Codec.INT.fieldOf("maximum_radius").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.maximumRadius),
					Codec.INT.fieldOf("maximum_rim").forGetter(deltaFeatureConfiguration -> deltaFeatureConfiguration.maximumRimSize)
				)
				.apply(instance, DeltaFeatureConfiguration::new)
	);
	public final BlockState contents;
	public final BlockState rim;
	public final int minimumRadius;
	public final int maximumRadius;
	public final int maximumRimSize;

	public DeltaFeatureConfiguration(BlockState blockState, BlockState blockState2, int i, int j, int k) {
		this.contents = blockState;
		this.rim = blockState2;
		this.minimumRadius = i;
		this.maximumRadius = j;
		this.maximumRimSize = k;
	}

	public static class Builder {
		Optional<BlockState> contents = Optional.empty();
		Optional<BlockState> rim = Optional.empty();
		int minRadius;
		int maxRadius;
		int maxRim;

		public DeltaFeatureConfiguration.Builder radius(int i, int j) {
			this.minRadius = i;
			this.maxRadius = j;
			return this;
		}

		public DeltaFeatureConfiguration.Builder contents(BlockState blockState) {
			this.contents = Optional.of(blockState);
			return this;
		}

		public DeltaFeatureConfiguration.Builder rim(BlockState blockState, int i) {
			this.rim = Optional.of(blockState);
			this.maxRim = i;
			return this;
		}

		public DeltaFeatureConfiguration build() {
			if (!this.contents.isPresent()) {
				throw new IllegalArgumentException("Missing contents");
			} else if (!this.rim.isPresent()) {
				throw new IllegalArgumentException("Missing rim");
			} else if (this.minRadius > this.maxRadius) {
				throw new IllegalArgumentException("Minimum radius cannot be greater than maximum radius");
			} else {
				return new DeltaFeatureConfiguration((BlockState)this.contents.get(), (BlockState)this.rim.get(), this.minRadius, this.maxRadius, this.maxRim);
			}
		}
	}
}
