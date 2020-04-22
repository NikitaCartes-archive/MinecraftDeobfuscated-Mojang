/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.featuresize;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.OptionalInt;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;

public abstract class FeatureSize {
    protected final FeatureSizeType<?> type;
    private final OptionalInt minClippedHeight;

    public FeatureSize(FeatureSizeType<?> featureSizeType, OptionalInt optionalInt) {
        this.type = featureSizeType;
        this.minClippedHeight = optionalInt;
    }

    public abstract int getSizeAtHeight(int var1, int var2);

    public OptionalInt minClippedHeight() {
        return this.minClippedHeight;
    }

    public <T> T serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.FEATURE_SIZE_TYPES.getKey(this.type).toString()));
        this.minClippedHeight.ifPresent(i -> builder.put(dynamicOps.createString("min_clipped_height"), dynamicOps.createInt(i)));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
    }
}

