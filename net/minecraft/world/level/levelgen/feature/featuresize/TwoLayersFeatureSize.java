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

public class TwoLayersFeatureSize
extends FeatureSize {
    public static final Codec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("limit")).withDefault(1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.limit), ((MapCodec)Codec.INT.fieldOf("lower_size")).withDefault(0).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.lowerSize), ((MapCodec)Codec.INT.fieldOf("upper_size")).withDefault(1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.upperSize), TwoLayersFeatureSize.minClippedHeightCodec()).apply((Applicative<TwoLayersFeatureSize, ?>)instance, TwoLayersFeatureSize::new));
    private final int limit;
    private final int lowerSize;
    private final int upperSize;

    public TwoLayersFeatureSize(int i, int j, int k) {
        this(i, j, k, OptionalInt.empty());
    }

    public TwoLayersFeatureSize(int i, int j, int k, OptionalInt optionalInt) {
        super(optionalInt);
        this.limit = i;
        this.lowerSize = j;
        this.upperSize = k;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int i, int j) {
        return j < this.limit ? this.lowerSize : this.upperSize;
    }
}

