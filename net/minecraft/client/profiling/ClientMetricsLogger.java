/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.profiling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(value=EnvType.CLIENT)
public interface ClientMetricsLogger {
    public void end();

    public void startTick();

    public boolean isRecording();

    public ProfilerFiller getProfiler();

    public void endTick();
}

