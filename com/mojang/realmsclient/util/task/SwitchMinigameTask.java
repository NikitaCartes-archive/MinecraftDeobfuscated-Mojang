/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(value=EnvType.CLIENT)
public class SwitchMinigameTask
extends LongRunningTask {
    private final long worldId;
    private final WorldTemplate worldTemplate;
    private final RealmsConfigureWorldScreen lastScreen;

    public SwitchMinigameTask(long l, WorldTemplate worldTemplate, RealmsConfigureWorldScreen realmsConfigureWorldScreen) {
        this.worldId = l;
        this.worldTemplate = worldTemplate;
        this.lastScreen = realmsConfigureWorldScreen;
    }

    @Override
    public void run() {
        RealmsClient realmsClient = RealmsClient.create();
        String string = I18n.get("mco.minigame.world.starting.screen.title", new Object[0]);
        this.setTitle(string);
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                if (!realmsClient.putIntoMinigameMode(this.worldId, this.worldTemplate.id).booleanValue()) continue;
                SwitchMinigameTask.setScreen(this.lastScreen);
                break;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                SwitchMinigameTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't start mini game!");
                this.error(exception.toString());
            }
        }
    }
}

