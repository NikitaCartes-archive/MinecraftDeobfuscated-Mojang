/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.registry;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.registry.MeasuredMetric;

public interface ProfilerMeasured {
    @Environment(value=EnvType.CLIENT)
    public List<MeasuredMetric> metrics();
}

