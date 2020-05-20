/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;

public class ThreeLayersFeatureSize
extends FeatureSize {
    public static final Codec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("limit")).withDefault(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.limit), ((MapCodec)Codec.INT.fieldOf("upper_limit")).withDefault(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperLimit), ((MapCodec)Codec.INT.fieldOf("lower_size")).withDefault(0).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.lowerSize), ((MapCodec)Codec.INT.fieldOf("middle_size")).withDefault(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.middleSize), ((MapCodec)Codec.INT.fieldOf("upper_size")).withDefault(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperSize), ThreeLayersFeatureSize.minClippedHeightCodec()).apply((Applicative<ThreeLayersFeatureSize, ?>)instance, ThreeLayersFeatureSize::new));
    private final int limit;
    private final int upperLimit;
    private final int lowerSize;
    private final int middleSize;
    private final int upperSize;

    public ThreeLayersFeatureSize(int i, int j, int k, int l, int m, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = i;
        this.upperLimit = j;
        this.lowerSize = k;
        this.middleSize = l;
        this.upperSize = m;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int i, int j) {
        if (j < this.limit) {
            return this.lowerSize;
        }
        if (j >= i - this.upperLimit) {
            return this.upperSize;
        }
        return this.middleSize;
    }
}

