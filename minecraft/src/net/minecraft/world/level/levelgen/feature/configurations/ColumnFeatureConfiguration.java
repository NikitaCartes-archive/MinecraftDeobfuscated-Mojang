package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
	public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("minimum_reach").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.minimumReach),
					Codec.INT.fieldOf("maximum_reach").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.maximumReach),
					Codec.INT.fieldOf("minimum_height").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.minimumHeight),
					Codec.INT.fieldOf("maximum_height").forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.maximumHeight)
				)
				.apply(instance, ColumnFeatureConfiguration::new)
	);
	public final int minimumReach;
	public final int maximumReach;
	public final int minimumHeight;
	public final int maximumHeight;

	public ColumnFeatureConfiguration(int i, int j, int k, int l) {
		this.minimumReach = i;
		this.maximumReach = j;
		this.minimumHeight = k;
		this.maximumHeight = l;
	}

	public static class Builder {
		private int minReach;
		private int maxReach;
		private int minHeight;
		private int maxHeight;

		public ColumnFeatureConfiguration.Builder horizontalReach(int i) {
			this.minReach = i;
			this.maxReach = i;
			return this;
		}

		public ColumnFeatureConfiguration.Builder horizontalReach(int i, int j) {
			this.minReach = i;
			this.maxReach = j;
			return this;
		}

		public ColumnFeatureConfiguration.Builder heightRange(int i, int j) {
			this.minHeight = i;
			this.maxHeight = j;
			return this;
		}

		public ColumnFeatureConfiguration build() {
			if (this.minHeight < 1) {
				throw new IllegalArgumentException("Minimum height cannot be less than 1");
			} else if (this.minReach < 0) {
				throw new IllegalArgumentException("Minimum reach cannot be negative");
			} else if (this.minReach <= this.maxReach && this.minHeight <= this.maxHeight) {
				return new ColumnFeatureConfiguration(this.minReach, this.maxReach, this.minHeight, this.maxHeight);
			} else {
				throw new IllegalArgumentException("Minimum reach/height cannot be greater than maximum width/height");
			}
		}
	}
}
