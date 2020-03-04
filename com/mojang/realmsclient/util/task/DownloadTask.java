/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

@Environment(value=EnvType.CLIENT)
public class DownloadTask
extends LongRunningTask {
    private final long worldId;
    private final int slot;
    private final Screen lastScreen;
    private final String downloadName;

    public DownloadTask(long l, int i, String string, Screen screen) {
        this.worldId = l;
        this.slot = i;
        this.lastScreen = screen;
        this.downloadName = string;
    }

    @Override
    public void run() {
        this.setTitle(I18n.get("mco.download.preparing", new Object[0]));
        RealmsClient realmsClient = RealmsClient.create();
        for (int i = 0; i < 25; ++i) {
            try {
                if (this.aborted()) {
                    return;
                }
                WorldDownload worldDownload = realmsClient.download(this.worldId, this.slot);
                DownloadTask.pause(1);
                if (this.aborted()) {
                    return;
                }
                DownloadTask.setScreen(new RealmsDownloadLatestWorldScreen(this.lastScreen, worldDownload, this.downloadName, bl -> {}));
                return;
            } catch (RetryCallException retryCallException) {
                if (this.aborted()) {
                    return;
                }
                DownloadTask.pause(retryCallException.delaySeconds);
                continue;
            } catch (RealmsServiceException realmsServiceException) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't download world data");
                DownloadTask.setScreen(new RealmsGenericErrorScreen(realmsServiceException, this.lastScreen));
                return;
            } catch (Exception exception) {
                if (this.aborted()) {
                    return;
                }
                LOGGER.error("Couldn't download world data", (Throwable)exception);
                this.error(exception.getLocalizedMessage());
                return;
            }
        }
    }
}

