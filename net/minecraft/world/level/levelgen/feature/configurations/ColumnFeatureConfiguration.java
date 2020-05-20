/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ColumnFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("minimum_reach")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.minimumReach), ((MapCodec)Codec.INT.fieldOf("maximum_reach")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.maximumReach), ((MapCodec)Codec.INT.fieldOf("minimum_height")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.minimumHeight), ((MapCodec)Codec.INT.fieldOf("maximum_height")).forGetter(columnFeatureConfiguration -> columnFeatureConfiguration.maximumHeight)).apply((Applicative<ColumnFeatureConfiguration, ?>)instance, ColumnFeatureConfiguration::new));
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

        public Builder horizontalReach(int i) {
            this.minReach = i;
            this.maxReach = i;
            return this;
        }

        public Builder horizontalReach(int i, int j) {
            this.minReach = i;
            this.maxReach = j;
            return this;
        }

        public Builder heightRange(int i, int j) {
            this.minHeight = i;
            this.maxHeight = j;
            return this;
        }

        public ColumnFeatureConfiguration build() {
            if (this.minHeight < 1) {
                throw new IllegalArgumentException("Minimum height cannot be less than 1");
            }
            if (this.minReach < 0) {
                throw new IllegalArgumentException("Minimum reach cannot be negative");
            }
            if (this.minReach > this.maxReach || this.minHeight > this.maxHeight) {
                throw new IllegalArgumentException("Minimum reach/height cannot be greater than maximum width/height");
            }
            return new ColumnFeatureConfiguration(this.minReach, this.maxReach, this.minHeight, this.maxHeight);
        }
    }
}

