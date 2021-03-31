/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.registry;

import java.util.List;
import net.minecraft.util.profiling.registry.MeasuredMetric;

public interface ProfilerMeasured {
    public List<MeasuredMetric> metrics();
}

