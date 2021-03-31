/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.registry.MeasuredMetric;
import net.minecraft.util.profiling.registry.MeasurementCategory;
import net.minecraft.util.profiling.registry.ProfilerMeasured;

public class MeasurementRegistry {
    public static final MeasurementRegistry INSTANCE = new MeasurementRegistry();
    private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap();

    private MeasurementRegistry() {
    }

    public void add(ProfilerMeasured profilerMeasured) {
        this.measuredInstances.put(profilerMeasured, null);
    }

    public Map<MeasurementCategory, List<MeasuredMetric>> getMetricsByCategories() {
        return this.measuredInstances.keySet().stream().flatMap(profilerMeasured -> profilerMeasured.metrics().stream()).collect(Collectors.collectingAndThen(Collectors.groupingBy(MeasuredMetric::getGetCategory), EnumMap::new));
    }
}

