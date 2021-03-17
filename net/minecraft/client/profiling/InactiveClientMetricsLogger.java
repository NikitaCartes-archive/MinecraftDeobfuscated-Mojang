/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.profiling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.profiling.ClientMetricsLogger;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(value=EnvType.CLIENT)
public class InactiveClientMetricsLogger
implements ClientMetricsLogger {
    public static final ClientMetricsLogger INSTANCE = new InactiveClientMetricsLogger();

    @Override
    public void end() {
    }

    @Override
    public void startTick() {
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public ProfilerFiller getProfiler() {
        return InactiveProfiler.INSTANCE;
    }

    @Override
    public void endTick() {
    }
}

