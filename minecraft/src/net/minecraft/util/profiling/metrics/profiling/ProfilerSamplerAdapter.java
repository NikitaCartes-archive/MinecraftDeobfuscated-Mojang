package net.minecraft.util.profiling.metrics.profiling;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;

public class ProfilerSamplerAdapter {
	private final Set<String> previouslyFoundSamplerNames = new ObjectOpenHashSet<>();

	public Set<MetricSampler> newSamplersFoundInProfiler(Supplier<ProfileCollector> supplier) {
		Set<MetricSampler> set = (Set<MetricSampler>)((ProfileCollector)supplier.get())
			.getChartedPaths()
			.stream()
			.filter(pair -> !this.previouslyFoundSamplerNames.contains(pair.getLeft()))
			.map(pair -> samplerForProfilingPath(supplier, (String)pair.getLeft(), (MetricCategory)pair.getRight()))
			.collect(Collectors.toSet());

		for (MetricSampler metricSampler : set) {
			this.previouslyFoundSamplerNames.add(metricSampler.getName());
		}

		return set;
	}

	private static MetricSampler samplerForProfilingPath(Supplier<ProfileCollector> supplier, String string, MetricCategory metricCategory) {
		return MetricSampler.create(string, metricCategory, () -> {
			ActiveProfiler.PathEntry pathEntry = ((ProfileCollector)supplier.get()).getEntry(string);
			return pathEntry == null ? 0.0 : (double)pathEntry.getMaxDuration() / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
		});
	}
}
