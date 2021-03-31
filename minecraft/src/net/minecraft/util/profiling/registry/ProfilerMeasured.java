package net.minecraft.util.profiling.registry;

import java.util.List;

public interface ProfilerMeasured {
	List<MeasuredMetric> metrics();
}
