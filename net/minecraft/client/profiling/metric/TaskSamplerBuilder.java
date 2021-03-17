/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.profiling.metric;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.profiling.metric.MetricSampler;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.registry.Metric;
import org.apache.commons.lang3.StringUtils;

@Environment(value=EnvType.CLIENT)
public class TaskSamplerBuilder {
    private final Metric metric;
    private final Supplier<ProfileCollector> profiler;

    public TaskSamplerBuilder(Metric metric, Supplier<ProfileCollector> supplier) {
        this.metric = metric;
        this.profiler = supplier;
    }

    public TaskSamplerBuilder(String string, Supplier<ProfileCollector> supplier) {
        this(new Metric(string), supplier);
    }

    public MetricSampler forPath(String ... strings) {
        if (strings.length == 0) {
            throw new IllegalArgumentException("Expected at least one path node, got no values");
        }
        String string = StringUtils.join((Object[])strings, '\u001e');
        return MetricSampler.create(this.metric, () -> {
            ActiveProfiler.PathEntry pathEntry = this.profiler.get().getEntry(string);
            return pathEntry == null ? -1.0 : (double)pathEntry.getDuration() / 1000000.0;
        });
    }
}

