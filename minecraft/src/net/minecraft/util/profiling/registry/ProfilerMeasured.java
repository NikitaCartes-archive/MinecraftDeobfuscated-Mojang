package net.minecraft.util.profiling.registry;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ProfilerMeasured {
	@Environment(EnvType.CLIENT)
	List<MeasuredMetric> metrics();
}
