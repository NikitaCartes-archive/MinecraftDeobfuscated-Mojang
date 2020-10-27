package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsBrokenWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsTermsScreen;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class GetServerDetailsTask extends LongRunningTask {
	private final RealmsServer server;
	private final Screen lastScreen;
	private final RealmsMainScreen mainScreen;
	private final ReentrantLock connectLock;

	public GetServerDetailsTask(RealmsMainScreen realmsMainScreen, Screen screen, RealmsServer realmsServer, ReentrantLock reentrantLock) {
		this.lastScreen = screen;
		this.mainScreen = realmsMainScreen;
		this.server = realmsServer;
		this.connectLock = reentrantLock;
	}

	public void run() {
		this.setTitle(new TranslatableComponent("mco.connect.connecting"));
		RealmsClient realmsClient = RealmsClient.create();
		boolean bl = false;
		boolean bl2 = false;
		int i = 5;
		RealmsServerAddress realmsServerAddress = null;
		boolean bl3 = false;
		boolean bl4 = false;

		for (int j = 0; j < 40 && !this.aborted(); j++) {
			try {
				realmsServerAddress = realmsClient.join(this.server.id);
				bl = true;
			} catch (RetryCallException var11) {
				i = var11.delaySeconds;
			} catch (RealmsServiceException var12) {
				if (var12.errorCode == 6002) {
					bl3 = true;
				} else if (var12.errorCode == 6006) {
					bl4 = true;
				} else {
					bl2 = true;
					this.error(var12.toString());
					LOGGER.error("Couldn't connect to world", (Throwable)var12);
				}
				break;
			} catch (Exception var13) {
				bl2 = true;
				LOGGER.error("Couldn't connect to world", (Throwable)var13);
				this.error(var13.getLocalizedMessage());
				break;
			}

			if (bl) {
				break;
			}

			this.sleep(i);
		}

		if (bl3) {
			setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
		} else if (bl4) {
			if (this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid())) {
				setScreen(new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME));
			} else {
				setScreen(
					new RealmsGenericErrorScreen(
						new TranslatableComponent("mco.brokenworld.nonowner.title"), new TranslatableComponent("mco.brokenworld.nonowner.error"), this.lastScreen
					)
				);
			}
		} else if (!this.aborted() && !bl2) {
			if (bl) {
				RealmsServerAddress realmsServerAddress2 = realmsServerAddress;
				if (realmsServerAddress2.resourcePackUrl != null && realmsServerAddress2.resourcePackHash != null) {
					Component component = new TranslatableComponent("mco.configure.world.resourcepack.question.line1");
					Component component2 = new TranslatableComponent("mco.configure.world.resourcepack.question.line2");
					setScreen(
						new RealmsLongConfirmationScreen(
							blx -> {
								try {
									if (blx) {
										Function<Throwable, Void> function = throwable -> {
											Minecraft.getInstance().getClientPackSource().clearServerPack();
											LOGGER.error(throwable);
											setScreen(new RealmsGenericErrorScreen(new TextComponent("Failed to download resource pack!"), this.lastScreen));
											return null;
										};

										try {
											Minecraft.getInstance()
												.getClientPackSource()
												.downloadAndSelectResourcePack(realmsServerAddress2.resourcePackUrl, realmsServerAddress2.resourcePackHash)
												.thenRun(
													() -> this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, realmsServerAddress2)))
												)
												.exceptionally(function);
										} catch (Exception var8x) {
											function.apply(var8x);
										}
									} else {
										setScreen(this.lastScreen);
									}
								} finally {
									if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
										this.connectLock.unlock();
									}
								}
							},
							RealmsLongConfirmationScreen.Type.Info,
							component,
							component2,
							true
						)
					);
				} else {
					this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, realmsServerAddress2)));
				}
			} else {
				this.error(new TranslatableComponent("mco.errorMessage.connectionFailure"));
			}
		}
	}

	private void sleep(int i) {
		try {
			Thread.sleep((long)(i * 1000));
		} catch (InterruptedException var3) {
			LOGGER.warn(var3.getLocalizedMessage());
		}
	}
}
