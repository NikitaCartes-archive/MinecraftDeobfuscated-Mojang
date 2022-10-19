/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.flag;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.jetbrains.annotations.Nullable;

public final class FeatureFlagSet {
    private static final FeatureFlagSet EMPTY = new FeatureFlagSet(null, 0L);
    public static final int MAX_CONTAINER_SIZE = 64;
    @Nullable
    private final FeatureFlagUniverse universe;
    private final long mask;

    private FeatureFlagSet(@Nullable FeatureFlagUniverse featureFlagUniverse, long l) {
        this.universe = featureFlagUniverse;
        this.mask = l;
    }

    static FeatureFlagSet create(FeatureFlagUniverse featureFlagUniverse, Collection<FeatureFlag> collection) {
        if (collection.isEmpty()) {
            return EMPTY;
        }
        long l = FeatureFlagSet.computeMask(featureFlagUniverse, 0L, collection);
        return new FeatureFlagSet(featureFlagUniverse, l);
    }

    public static FeatureFlagSet of() {
        return EMPTY;
    }

    public static FeatureFlagSet of(FeatureFlag featureFlag) {
        return new FeatureFlagSet(featureFlag.universe, featureFlag.mask);
    }

    public static FeatureFlagSet of(FeatureFlag featureFlag, FeatureFlag ... featureFlags) {
        long l = featureFlags.length == 0 ? featureFlag.mask : FeatureFlagSet.computeMask(featureFlag.universe, featureFlag.mask, Arrays.asList(featureFlags));
        return new FeatureFlagSet(featureFlag.universe, l);
    }

    private static long computeMask(FeatureFlagUniverse featureFlagUniverse, long l, Iterable<FeatureFlag> iterable) {
        for (FeatureFlag featureFlag : iterable) {
            if (featureFlagUniverse != featureFlag.universe) {
                throw new IllegalStateException("Mismatched feature universe, expected '" + featureFlagUniverse + "', but got '" + featureFlag.universe + "'");
            }
            l |= featureFlag.mask;
        }
        return l;
    }

    public boolean contains(FeatureFlag featureFlag) {
        if (this.universe != featureFlag.universe) {
            return false;
        }
        return (this.mask & featureFlag.mask) != 0L;
    }

    public boolean isSubsetOf(FeatureFlagSet featureFlagSet) {
        if (this.universe == null) {
            return true;
        }
        if (this.universe != featureFlagSet.universe) {
            return false;
        }
        return (this.mask & (featureFlagSet.mask ^ 0xFFFFFFFFFFFFFFFFL)) == 0L;
    }

    public FeatureFlagSet join(FeatureFlagSet featureFlagSet) {
        if (this.universe == null) {
            return featureFlagSet;
        }
        if (featureFlagSet.universe == null) {
            return this;
        }
        if (this.universe != featureFlagSet.universe) {
            throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + featureFlagSet.universe + "'");
        }
        return new FeatureFlagSet(this.universe, this.mask | featureFlagSet.mask);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof FeatureFlagSet)) return false;
        FeatureFlagSet featureFlagSet = (FeatureFlagSet)object;
        if (this.universe != featureFlagSet.universe) return false;
        if (this.mask != featureFlagSet.mask) return false;
        return true;
    }

    public int hashCode() {
        return (int)HashCommon.mix(this.mask);
    }
}

