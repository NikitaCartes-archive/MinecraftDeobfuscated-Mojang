/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

@Environment(value=EnvType.CLIENT)
public class RestoreTask
extends LongRunningTask {
    private final Backup backup;
    private final long worldId;
    private final RealmsConfigureWorldScreen lastScreen;

    public RestoreTask(Backup backup, long l, RealmsConfigureWorldScreen realmsConfigureWorldScreen) {
        this.backup = backup;
        this.worldId = l;
        this.lastScreen = realmsConfigureWorldScreen;
    }

    @Override
    public void run() {
        this.setTitle(I18n.get("mco.backup.restoring", new Object[0]));
        RealmsClient realmsClient = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                realmsClient.restoreWorld(this.worldId, this.backup.backupId);
                RestoreTask.pause(1);
                if (this.aborted()) {
                    return;
                }
                RestoreTask.setScreen(this.lastScreen.getNewScreen());
                return;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                RestoreTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (RealmsServiceException realmsServiceException) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't restore backup", (Throwable)realmsServiceException);
                RestoreTask.setScreen(new RealmsGenericErrorScreen(realmsServiceException, (Screen)this.lastScreen));
                return;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't restore backup", (Throwable)exception);
                this.error(exception.getLocalizedMessage());
                return;
            }
        }
    }
}

