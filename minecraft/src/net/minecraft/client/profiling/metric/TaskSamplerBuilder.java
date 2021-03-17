package net.minecraft.client.profiling.metric;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.registry.Metric;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
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

	public MetricSampler forPath(String... strings) {
		if (strings.length == 0) {
			throw new IllegalArgumentException("Expected at least one path node, got no values");
		} else {
			String string = StringUtils.join((Object[])strings, '\u001e');
			return MetricSampler.create(this.metric, () -> {
				ActiveProfiler.PathEntry pathEntry = ((ProfileCollector)this.profiler.get()).getEntry(string);
				return pathEntry == null ? -1.0 : (double)pathEntry.getDuration() / 1000000.0;
			});
		}
	}
}
