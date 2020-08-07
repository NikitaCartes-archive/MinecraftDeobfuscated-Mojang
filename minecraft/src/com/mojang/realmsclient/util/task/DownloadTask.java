package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class DownloadTask extends LongRunningTask {
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

	public void run() {
		this.setTitle(new TranslatableComponent("mco.download.preparing"));
		RealmsClient realmsClient = RealmsClient.create();
		int i = 0;

		while (i < 25) {
			try {
				if (this.aborted()) {
					return;
				}

				WorldDownload worldDownload = realmsClient.requestDownloadInfo(this.worldId, this.slot);
				pause(1);
				if (this.aborted()) {
					return;
				}

				setScreen(new RealmsDownloadLatestWorldScreen(this.lastScreen, worldDownload, this.downloadName, bl -> {
				}));
				return;
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause(var4.delaySeconds);
				i++;
			} catch (RealmsServiceException var5) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't download world data");
				setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
				return;
			} catch (Exception var6) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't download world data", (Throwable)var6);
				this.error(var6.getLocalizedMessage());
				return;
			}
		}
	}
}
