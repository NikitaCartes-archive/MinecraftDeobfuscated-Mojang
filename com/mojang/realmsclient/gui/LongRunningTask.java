/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public abstract class LongRunningTask
implements Runnable {
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

    public void setScreen(RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen) {
        this.longRunningMcoTaskScreen = realmsLongRunningMcoTaskScreen;
    }

    public void error(String string) {
        this.longRunningMcoTaskScreen.error(string);
    }

    public void setTitle(String string) {
        this.longRunningMcoTaskScreen.setTitle(string);
    }

    public boolean aborted() {
        return this.longRunningMcoTaskScreen.aborted();
    }

    public void tick() {
    }

    public void init() {
    }

    public void abortTask() {
    }
}

