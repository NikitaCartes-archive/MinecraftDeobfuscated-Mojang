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
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

		RealmsServerAddress realmsServerAddress;
		try {
			realmsServerAddress = this.fetchServerAddress();
		} catch (CancellationException var4) {
			LOGGER.info("User aborted connecting to realms");
			return;
		} catch (RealmsServiceException var5) {
			switch (var5.errorCode) {
				case 6002:
					setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
					return;
				case 6006:
					boolean bl = this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid());
					setScreen(
						(Screen)(bl
							? new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME)
							: new RealmsGenericErrorScreen(
								new TranslatableComponent("mco.brokenworld.nonowner.title"), new TranslatableComponent("mco.brokenworld.nonowner.error"), this.lastScreen
							))
					);
					return;
				default:
					this.error(var5.toString());
					LOGGER.error("Couldn't connect to world", (Throwable)var5);
					return;
			}
		} catch (TimeoutException var6) {
			this.error(new TranslatableComponent("mco.errorMessage.connectionFailure"));
			return;
		} catch (Exception var7) {
			LOGGER.error("Couldn't connect to world", (Throwable)var7);
			this.error(var7.getLocalizedMessage());
			return;
		}

		boolean bl2 = realmsServerAddress.resourcePackUrl != null && realmsServerAddress.resourcePackHash != null;
		Screen screen = (Screen)(bl2
			? this.resourcePackDownloadConfirmationScreen(realmsServerAddress, this::connectScreen)
			: this.connectScreen(realmsServerAddress));
		setScreen(screen);
	}

	private RealmsServerAddress fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
		RealmsClient realmsClient = RealmsClient.create();

		for (int i = 0; i < 40; i++) {
			if (this.aborted()) {
				throw new CancellationException();
			}

			try {
				return realmsClient.join(this.server.id);
			} catch (RetryCallException var4) {
				pause((long)var4.delaySeconds);
			}
		}

		throw new TimeoutException();
	}

	public RealmsLongRunningMcoTaskScreen connectScreen(RealmsServerAddress realmsServerAddress) {
		return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, realmsServerAddress));
	}

	private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(
		RealmsServerAddress realmsServerAddress, Function<RealmsServerAddress, Screen> function
	) {
		BooleanConsumer booleanConsumer = bl -> {
			try {
				if (bl) {
					this.scheduleResourcePackDownload(realmsServerAddress).thenRun(() -> setScreen((Screen)function.apply(realmsServerAddress))).exceptionally(throwable -> {
						Minecraft.getInstance().getClientPackSource().clearServerPack();
						LOGGER.error(throwable);
						setScreen(new RealmsGenericErrorScreen(new TextComponent("Failed to download resource pack!"), this.lastScreen));
						return null;
					});
					return;
				}

				setScreen(this.lastScreen);
			} finally {
				if (this.connectLock.isHeldByCurrentThread()) {
					this.connectLock.unlock();
				}
			}
		};
		return new RealmsLongConfirmationScreen(
			booleanConsumer,
			RealmsLongConfirmationScreen.Type.Info,
			new TranslatableComponent("mco.configure.world.resourcepack.question.line1"),
			new TranslatableComponent("mco.configure.world.resourcepack.question.line2"),
			true
		);
	}

	private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress realmsServerAddress) {
		try {
			return Minecraft.getInstance()
				.getClientPackSource()
				.downloadAndSelectResourcePack(realmsServerAddress.resourcePackUrl, realmsServerAddress.resourcePackHash);
		} catch (Exception var4) {
			CompletableFuture<Void> completableFuture = new CompletableFuture();
			completableFuture.completeExceptionally(var4);
			return completableFuture;
		}
	}
}
