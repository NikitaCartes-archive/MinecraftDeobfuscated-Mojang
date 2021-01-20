/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.realmsclient.gui.task.RestartDelayCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class NoStartupDelay
implements RestartDelayCalculator {
    @Override
    public void markExecutionStart() {
    }

    @Override
    public long getNextDelayMs() {
        return 0L;
    }
}

