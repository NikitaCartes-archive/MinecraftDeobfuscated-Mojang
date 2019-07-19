/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

public interface ProgressListener {
    public void progressStartNoAbort(Component var1);

    @Environment(value=EnvType.CLIENT)
    public void progressStart(Component var1);

    public void progressStage(Component var1);

    public void progressStagePercentage(int var1);

    @Environment(value=EnvType.CLIENT)
    public void stop();
}

