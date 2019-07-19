/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ProfilerFiller {
    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void popPush(String var1);

    @Environment(value=EnvType.CLIENT)
    public void popPush(Supplier<String> var1);
}

