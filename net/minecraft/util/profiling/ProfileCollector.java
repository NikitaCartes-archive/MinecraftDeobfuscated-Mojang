/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import java.util.Set;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public interface ProfileCollector
extends ProfilerFiller {
    public ProfileResults getResults();

    @Nullable
    public ActiveProfiler.PathEntry getEntry(String var1);

    public Set<Pair<String, MetricCategory>> getChartedPaths();
}

