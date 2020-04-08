/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class DeltaFeatureConfiguration
implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(new ImmutableMap.Builder<T, T>().put(dynamicOps.createString("contents"), BlockState.serialize(dynamicOps, this.contents).getValue()).put(dynamicOps.createString("rim"), BlockState.serialize(dynamicOps, this.rim).getValue()).put(dynamicOps.createString("minimum_radius"), dynamicOps.createInt(this.minimumRadius)).put(dynamicOps.createString("maximum_radius"), dynamicOps.createInt(this.maximumRadius)).put(dynamicOps.createString("maximum_rim"), dynamicOps.createInt(this.maximumRimSize)).build()));
    }

    public static <T> DeltaFeatureConfiguration deserialize(Dynamic<T> dynamic) {
        BlockState blockState = dynamic.get("contents").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState blockState2 = dynamic.get("rim").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int i = dynamic.get("minimum_radius").asInt(0);
        int j = dynamic.get("maximum_radius").asInt(0);
        int k = dynamic.get("maximum_rim").asInt(0);
        return new DeltaFeatureConfiguration(blockState, blockState2, i, j, k);
    }

    public static class Builder {
        Optional<BlockState> contents = Optional.empty();
        Optional<BlockState> rim = Optional.empty();
        int minRadius;
        int maxRadius;
        int maxRim;

        public Builder radius(int i, int j) {
            this.minRadius = i;
            this.maxRadius = j;
            return this;
        }

        public Builder contents(BlockState blockState) {
            this.contents = Optional.of(blockState);
            return this;
        }

        public Builder rim(BlockState blockState, int i) {
            this.rim = Optional.of(blockState);
            this.maxRim = i;
            return this;
        }

        public DeltaFeatureConfiguration build() {
            if (!this.contents.isPresent()) {
                throw new IllegalArgumentException("Missing contents");
            }
            if (!this.rim.isPresent()) {
                throw new IllegalArgumentException("Missing rim");
            }
            if (this.minRadius > this.maxRadius) {
                throw new IllegalArgumentException("Minimum radius cannot be greater than maximum radius");
            }
            return new DeltaFeatureConfiguration(this.contents.get(), this.rim.get(), this.minRadius, this.maxRadius, this.maxRim);
        }
    }
}

