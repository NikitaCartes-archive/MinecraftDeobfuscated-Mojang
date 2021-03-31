/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.registry;

import java.util.function.DoubleSupplier;
import net.minecraft.util.profiling.registry.MeasurementCategory;
import net.minecraft.util.profiling.registry.Metric;

public class MeasuredMetric {
    private final Metric metric;
    private final DoubleSupplier currentValue;
    private final MeasurementCategory getCategory;

    public MeasuredMetric(Metric metric, DoubleSupplier doubleSupplier, MeasurementCategory measurementCategory) {
        this.metric = metric;
        this.currentValue = doubleSupplier;
        this.getCategory = measurementCategory;
    }

    public Metric getMetric() {
        return this.metric;
    }

    public DoubleSupplier getCurrentValue() {
        return this.currentValue;
    }

    public MeasurementCategory getGetCategory() {
        return this.getCategory;
    }
}

