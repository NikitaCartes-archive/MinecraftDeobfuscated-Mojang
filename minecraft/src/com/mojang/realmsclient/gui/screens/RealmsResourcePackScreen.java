package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.util.RealmsTasks;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsResourcePackScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsScreen lastScreen;
	private final RealmsServerAddress serverAddress;
	private final ReentrantLock connectLock;

	public RealmsResourcePackScreen(RealmsScreen realmsScreen, RealmsServerAddress realmsServerAddress, ReentrantLock reentrantLock) {
		this.lastScreen = realmsScreen;
		this.serverAddress = realmsServerAddress;
		this.connectLock = reentrantLock;
	}

	@Override
	public void confirmResult(boolean bl, int i) {
		try {
			if (!bl) {
				Realms.setScreen(this.lastScreen);
			} else {
				try {
					Realms.downloadResourcePack(this.serverAddress.resourcePackUrl, this.serverAddress.resourcePackHash)
						.thenRun(
							() -> {
								RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(
									this.lastScreen, new RealmsTasks.RealmsConnectTask(this.lastScreen, this.serverAddress)
								);
								realmsLongRunningMcoTaskScreen.start();
								Realms.setScreen(realmsLongRunningMcoTaskScreen);
							}
						)
						.exceptionally(throwable -> {
							Realms.clearResourcePack();
							LOGGER.error(throwable);
							Realms.setScreen(new RealmsGenericErrorScreen("Failed to download resource pack!", this.lastScreen));
							return null;
						});
				} catch (Exception var7) {
					Realms.clearResourcePack();
					LOGGER.error(var7);
					Realms.setScreen(new RealmsGenericErrorScreen("Failed to download resource pack!", this.lastScreen));
				}
			}
		} finally {
			if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
				this.connectLock.unlock();
			}
		}
	}
}
