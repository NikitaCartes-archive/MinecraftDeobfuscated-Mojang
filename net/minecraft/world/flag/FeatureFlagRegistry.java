/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.flag;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FeatureFlagUniverse universe;
    private final Map<ResourceLocation, FeatureFlag> names;
    private final FeatureFlagSet allFlags;

    FeatureFlagRegistry(FeatureFlagUniverse featureFlagUniverse, FeatureFlagSet featureFlagSet, Map<ResourceLocation, FeatureFlag> map) {
        this.universe = featureFlagUniverse;
        this.names = map;
        this.allFlags = featureFlagSet;
    }

    public boolean isSubset(FeatureFlagSet featureFlagSet) {
        return featureFlagSet.isSubsetOf(this.allFlags);
    }

    public FeatureFlagSet allFlags() {
        return this.allFlags;
    }

    public FeatureFlagSet fromNames(Iterable<ResourceLocation> iterable) {
        return this.fromNames(iterable, resourceLocation -> LOGGER.warn("Unknown feature flag: {}", resourceLocation));
    }

    public FeatureFlagSet subset(FeatureFlag ... featureFlags) {
        return FeatureFlagSet.create(this.universe, Arrays.asList(featureFlags));
    }

    public FeatureFlagSet fromNames(Iterable<ResourceLocation> iterable, Consumer<ResourceLocation> consumer) {
        Set<FeatureFlag> set = Sets.newIdentityHashSet();
        for (ResourceLocation resourceLocation : iterable) {
            FeatureFlag featureFlag = this.names.get(resourceLocation);
            if (featureFlag == null) {
                consumer.accept(resourceLocation);
                continue;
            }
            set.add(featureFlag);
        }
        return FeatureFlagSet.create(this.universe, set);
    }

    public Set<ResourceLocation> toNames(FeatureFlagSet featureFlagSet) {
        HashSet<ResourceLocation> set = new HashSet<ResourceLocation>();
        this.names.forEach((resourceLocation, featureFlag) -> {
            if (featureFlagSet.contains((FeatureFlag)featureFlag)) {
                set.add((ResourceLocation)resourceLocation);
            }
        });
        return set;
    }

    public Codec<FeatureFlagSet> codec() {
        return ResourceLocation.CODEC.listOf().comapFlatMap(list -> {
            HashSet set = new HashSet();
            FeatureFlagSet featureFlagSet = this.fromNames((Iterable<ResourceLocation>)list, set::add);
            if (!set.isEmpty()) {
                return DataResult.error(() -> "Unknown feature ids: " + set, featureFlagSet);
            }
            return DataResult.success(featureFlagSet);
        }, featureFlagSet -> List.copyOf(this.toNames((FeatureFlagSet)featureFlagSet)));
    }

    public static class Builder {
        private final FeatureFlagUniverse universe;
        private int id;
        private final Map<ResourceLocation, FeatureFlag> flags = new LinkedHashMap<ResourceLocation, FeatureFlag>();

        public Builder(String string) {
            this.universe = new FeatureFlagUniverse(string);
        }

        public FeatureFlag createVanilla(String string) {
            return this.create(new ResourceLocation("minecraft", string));
        }

        public FeatureFlag create(ResourceLocation resourceLocation) {
            FeatureFlag featureFlag;
            FeatureFlag featureFlag2;
            if (this.id >= 64) {
                throw new IllegalStateException("Too many feature flags");
            }
            if ((featureFlag2 = this.flags.put(resourceLocation, featureFlag = new FeatureFlag(this.universe, this.id++))) != null) {
                throw new IllegalStateException("Duplicate feature flag " + resourceLocation);
            }
            return featureFlag;
        }

        public FeatureFlagRegistry build() {
            FeatureFlagSet featureFlagSet = FeatureFlagSet.create(this.universe, this.flags.values());
            return new FeatureFlagRegistry(this.universe, featureFlagSet, Map.copyOf(this.flags));
        }
    }
}

