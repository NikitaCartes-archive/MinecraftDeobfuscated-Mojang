/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ResettingWorldTask
extends LongRunningTask {
    private final String seed;
    private final WorldTemplate worldTemplate;
    private final int levelType;
    private final boolean generateStructures;
    private final long serverId;
    private Component title = new TranslatableComponent("mco.reset.world.resetting.screen.title");
    private final Runnable callback;

    public ResettingWorldTask(@Nullable String string, @Nullable WorldTemplate worldTemplate, int i, boolean bl, long l, @Nullable Component component, Runnable runnable) {
        this.seed = string;
        this.worldTemplate = worldTemplate;
        this.levelType = i;
        this.generateStructures = bl;
        this.serverId = l;
        if (component != null) {
            this.title = component;
        }
        this.callback = runnable;
    }

    @Override
    public void run() {
        RealmsClient realmsClient = RealmsClient.create();
        this.setTitle(this.title);
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                if (this.worldTemplate != null) {
                    realmsClient.resetWorldWithTemplate(this.serverId, this.worldTemplate.id);
                } else {
                    realmsClient.resetWorldWithSeed(this.serverId, this.seed, this.levelType, this.generateStructures);
                }
                if (this.aborted()) {
                    return;
                }
                this.callback.run();
                return;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                ResettingWorldTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't reset world");
                this.error(exception.toString());
                return;
            }
        }
    }
}

