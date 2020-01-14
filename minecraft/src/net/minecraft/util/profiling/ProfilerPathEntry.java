package net.minecraft.util.profiling;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface ProfilerPathEntry {
	long getDuration();

	long getCount();

	Object2LongMap<String> getCounters();
}
