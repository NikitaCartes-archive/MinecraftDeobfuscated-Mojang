package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RestoreTask extends LongRunningTask {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Backup backup;
	private final long worldId;
	private final RealmsConfigureWorldScreen lastScreen;

	public RestoreTask(Backup backup, long l, RealmsConfigureWorldScreen realmsConfigureWorldScreen) {
		this.backup = backup;
		this.worldId = l;
		this.lastScreen = realmsConfigureWorldScreen;
	}

	public void run() {
		this.setTitle(Component.translatable("mco.backup.restoring"));
		RealmsClient realmsClient = RealmsClient.create();
		int i = 0;

		while (i < 25) {
			try {
				if (this.aborted()) {
					return;
				}

				realmsClient.restoreWorld(this.worldId, this.backup.backupId);
				pause(1L);
				if (this.aborted()) {
					return;
				}

				setScreen(this.lastScreen.getNewScreen());
				return;
			} catch (RetryCallException var4) {
				if (this.aborted()) {
					return;
				}

				pause((long)var4.delaySeconds);
				i++;
			} catch (RealmsServiceException var5) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't restore backup", (Throwable)var5);
				setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
				return;
			} catch (Exception var6) {
				if (this.aborted()) {
					return;
				}

				LOGGER.error("Couldn't restore backup", (Throwable)var6);
				this.error(var6);
				return;
			}
		}
	}
}
