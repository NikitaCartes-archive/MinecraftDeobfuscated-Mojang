package net.minecraft.util.profiling;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ProfileCollector extends ProfilerFiller {
	ProfileResults getResults();

	@Nullable
	@Environment(EnvType.CLIENT)
	ActiveProfiler.PathEntry getEntry(String string);
}
