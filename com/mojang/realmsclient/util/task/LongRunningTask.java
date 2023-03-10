/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class LongRunningTask
implements ErrorCallback,
Runnable {
    protected static final int NUMBER_OF_RETRIES = 25;
    private static final Logger LOGGER = LogUtils.getLogger();
    protected RealmsLongRunningMcoTaskScreen longRunningMcoTaskScreen;

    protected static void pause(long l) {
        try {
            Thread.sleep(l * 1000L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            LOGGER.error("", interruptedException);
        }
    }

    public static void setScreen(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(screen));
    }

    public void setScreen(RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen) {
        this.longRunningMcoTaskScreen = realmsLongRunningMcoTaskScreen;
    }

    @Override
    public void error(Component component) {
        this.longRunningMcoTaskScreen.error(component);
    }

    public void setTitle(Component component) {
        this.longRunningMcoTaskScreen.setTitle(component);
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

