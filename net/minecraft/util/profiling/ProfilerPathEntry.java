/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface ProfilerPathEntry {
    public long getDuration();

    public long getMaxDuration();

    public long getCount();

    public Object2LongMap<String> getCounters();
}

