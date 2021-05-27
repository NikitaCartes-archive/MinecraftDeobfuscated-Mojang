/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.metrics.profiling;

import net.minecraft.util.profiling.ProfilerFiller;

public interface MetricsRecorder {
    public void end();

    public void startTick();

    public boolean isRecording();

    public ProfilerFiller getProfiler();

    public void endTick();
}

