/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.TranslatableComponent;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SwitchSlotTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldId;
    private final int slot;
    private final Runnable callback;

    public SwitchSlotTask(long l, int i, Runnable runnable) {
        this.worldId = l;
        this.slot = i;
        this.callback = runnable;
    }

    @Override
    public void run() {
        RealmsClient realmsClient = RealmsClient.create();
        this.setTitle(new TranslatableComponent("mco.minigame.world.slot.screen.title"));
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                if (!realmsClient.switchSlot(this.worldId, this.slot)) continue;
                this.callback.run();
                break;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                SwitchSlotTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't switch world!");
                this.error(exception.toString());
            }
        }
    }
}

