package net.minecraft.util.profiling;

import javax.annotation.Nullable;

public interface ProfileCollector extends ProfilerFiller {
	ProfileResults getResults();

	@Nullable
	ActiveProfiler.PathEntry getEntry(String string);
}
