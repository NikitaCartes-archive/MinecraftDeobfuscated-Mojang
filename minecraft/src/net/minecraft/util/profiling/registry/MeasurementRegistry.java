package net.minecraft.util.profiling.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class MeasurementRegistry {
	public static final MeasurementRegistry INSTANCE = new MeasurementRegistry();
	private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap();

	private MeasurementRegistry() {
	}

	public void add(ProfilerMeasured profilerMeasured) {
		this.measuredInstances.put(profilerMeasured, null);
	}

	@Environment(EnvType.CLIENT)
	public Map<MeasurementCategory, List<MeasuredMetric>> getMetricsByCategories() {
		return (Map<MeasurementCategory, List<MeasuredMetric>>)this.measuredInstances
			.keySet()
			.stream()
			.flatMap(profilerMeasured -> profilerMeasured.metrics().stream())
			.collect(Collectors.collectingAndThen(Collectors.groupingBy(MeasuredMetric::getGetCategory), EnumMap::new));
	}
}
