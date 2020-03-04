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
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ResettingWorldTask
extends LongRunningTask {
    private final String seed;
    private final WorldTemplate worldTemplate;
    private final int levelType;
    private final boolean generateStructures;
    private final long serverId;
    private String title = I18n.get("mco.reset.world.resetting.screen.title", new Object[0]);
    private final Runnable callback;

    public ResettingWorldTask(@Nullable String string, @Nullable WorldTemplate worldTemplate, int i, boolean bl, long l, @Nullable String string2, Runnable runnable) {
        this.seed = string;
        this.worldTemplate = worldTemplate;
        this.levelType = i;
        this.generateStructures = bl;
        this.serverId = l;
        if (string2 != null) {
            this.title = string2;
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

