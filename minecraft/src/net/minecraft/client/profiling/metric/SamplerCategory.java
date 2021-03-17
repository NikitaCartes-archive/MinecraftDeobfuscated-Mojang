package net.minecraft.client.profiling.metric;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SamplerCategory {
	private final String name;
	private final MetricSampler[] metricSamplers;

	public SamplerCategory(String string, MetricSampler... metricSamplers) {
		this.name = string;
		this.metricSamplers = metricSamplers;
	}

	public SamplerCategory(String string, List<MetricSampler> list) {
		this.name = string;
		this.metricSamplers = (MetricSampler[])list.toArray(new MetricSampler[0]);
	}

	public void onEndTick() {
		for (MetricSampler metricSampler : this.metricSamplers) {
			metricSampler.onEndTick();
		}
	}

	public void onStartTick() {
		for (MetricSampler metricSampler : this.metricSamplers) {
			metricSampler.onStartTick();
		}
	}

	public void onFinished() {
		for (MetricSampler metricSampler : this.metricSamplers) {
			metricSampler.onFinished();
		}
	}

	public String getName() {
		return this.name;
	}

	public List<MetricSampler> getMetricSamplers() {
		return ImmutableList.copyOf(this.metricSamplers);
	}
}
