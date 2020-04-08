package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
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

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("minimum_reach"),
					dynamicOps.createInt(this.minimumReach),
					dynamicOps.createString("maximum_reach"),
					dynamicOps.createInt(this.maximumReach),
					dynamicOps.createString("minimum_height"),
					dynamicOps.createInt(this.minimumHeight),
					dynamicOps.createString("maximum_height"),
					dynamicOps.createInt(this.maximumHeight)
				)
			)
		);
	}

	public static <T> ColumnFeatureConfiguration deserialize(Dynamic<T> dynamic) {
		int i = dynamic.get("minimum_reach").asInt(0);
		int j = dynamic.get("maximum_reach").asInt(0);
		int k = dynamic.get("minimum_height").asInt(1);
		int l = dynamic.get("maximum_height").asInt(1);
		return new ColumnFeatureConfiguration(i, j, k, l);
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
