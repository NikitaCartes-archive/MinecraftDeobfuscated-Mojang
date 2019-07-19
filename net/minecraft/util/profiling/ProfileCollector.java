/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ProfileCollector
extends ProfilerFiller {
    @Override
    public void push(String var1);

    @Override
    public void push(Supplier<String> var1);

    @Override
    public void pop();

    @Override
    public void popPush(String var1);

    @Override
    @Environment(value=EnvType.CLIENT)
    public void popPush(Supplier<String> var1);

    public ProfileResults getResults();
}

