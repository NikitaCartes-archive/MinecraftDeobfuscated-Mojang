/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OpenServerTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RealmsServer serverData;
    private final Screen returnScreen;
    private final boolean join;
    private final RealmsMainScreen mainScreen;
    private final Minecraft minecraft;

    public OpenServerTask(RealmsServer realmsServer, Screen screen, RealmsMainScreen realmsMainScreen, boolean bl, Minecraft minecraft) {
        this.serverData = realmsServer;
        this.returnScreen = screen;
        this.join = bl;
        this.mainScreen = realmsMainScreen;
        this.minecraft = minecraft;
    }

    @Override
    public void run() {
        this.setTitle(Component.translatable("mco.configure.world.opening"));
        RealmsClient realmsClient = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            if (this.aborted()) {
                return;
            }
            try {
                boolean bl = realmsClient.open(this.serverData.id);
                if (!bl) continue;
                this.minecraft.execute(() -> {
                    if (this.returnScreen instanceof RealmsConfigureWorldScreen) {
                        ((RealmsConfigureWorldScreen)this.returnScreen).stateChanged();
                    }
                    this.serverData.state = RealmsServer.State.OPEN;
                    if (this.join) {
                        this.mainScreen.play(this.serverData, this.returnScreen);
                    } else {
                        this.minecraft.setScreen(this.returnScreen);
                    }
                });
                break;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                OpenServerTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Failed to open server", exception);
                this.error("Failed to open the server");
            }
        }
    }
}

