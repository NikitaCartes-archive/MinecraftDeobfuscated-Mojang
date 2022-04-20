/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
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
import com.mojang.realmsclient.util.task.ConnectTask;
import com.mojang.realmsclient.util.task.LongRunningTask;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GetServerDetailsTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
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

    @Override
    public void run() {
        RealmsServerAddress realmsServerAddress;
        this.setTitle(Component.translatable("mco.connect.connecting"));
        try {
            realmsServerAddress = this.fetchServerAddress();
        } catch (CancellationException cancellationException) {
            LOGGER.info("User aborted connecting to realms");
            return;
        } catch (RealmsServiceException realmsServiceException) {
            switch (realmsServiceException.realmsErrorCodeOrDefault(-1)) {
                case 6002: {
                    GetServerDetailsTask.setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
                    return;
                }
                case 6006: {
                    boolean bl = this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid());
                    GetServerDetailsTask.setScreen(bl ? new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id, this.server.worldType == RealmsServer.WorldType.MINIGAME) : new RealmsGenericErrorScreen(Component.translatable("mco.brokenworld.nonowner.title"), Component.translatable("mco.brokenworld.nonowner.error"), this.lastScreen));
                    return;
                }
            }
            this.error(realmsServiceException.toString());
            LOGGER.error("Couldn't connect to world", realmsServiceException);
            return;
        } catch (TimeoutException timeoutException) {
            this.error(Component.translatable("mco.errorMessage.connectionFailure"));
            return;
        } catch (Exception exception) {
            LOGGER.error("Couldn't connect to world", exception);
            this.error(exception.getLocalizedMessage());
            return;
        }
        boolean bl2 = realmsServerAddress.resourcePackUrl != null && realmsServerAddress.resourcePackHash != null;
        RealmsLongRunningMcoTaskScreen screen = bl2 ? this.resourcePackDownloadConfirmationScreen(realmsServerAddress, this::connectScreen) : this.connectScreen(realmsServerAddress);
        GetServerDetailsTask.setScreen(screen);
    }

    private RealmsServerAddress fetchServerAddress() throws RealmsServiceException, TimeoutException, CancellationException {
        RealmsClient realmsClient = RealmsClient.create();
        for (int i = 0; i < 40; ++i) {
            if (this.aborted()) {
                throw new CancellationException();
            }
            try {
                return realmsClient.join(this.server.id);
            } catch (RetryCallException retryCallException) {
                GetServerDetailsTask.pause(retryCallException.delaySeconds);
                continue;
            }
        }
        throw new TimeoutException();
    }

    public RealmsLongRunningMcoTaskScreen connectScreen(RealmsServerAddress realmsServerAddress) {
        return new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, this.server, realmsServerAddress));
    }

    private RealmsLongConfirmationScreen resourcePackDownloadConfirmationScreen(RealmsServerAddress realmsServerAddress, Function<RealmsServerAddress, Screen> function) {
        BooleanConsumer booleanConsumer = bl -> {
            try {
                if (!bl) {
                    GetServerDetailsTask.setScreen(this.lastScreen);
                    return;
                }
                ((CompletableFuture)this.scheduleResourcePackDownload(realmsServerAddress).thenRun(() -> GetServerDetailsTask.setScreen((Screen)function.apply(realmsServerAddress)))).exceptionally(throwable -> {
                    Minecraft.getInstance().getClientPackSource().clearServerPack();
                    LOGGER.error("Failed to download resource pack from {}", (Object)realmsServerAddress, throwable);
                    GetServerDetailsTask.setScreen(new RealmsGenericErrorScreen(Component.literal("Failed to download resource pack!"), this.lastScreen));
                    return null;
                });
            } finally {
                if (this.connectLock.isHeldByCurrentThread()) {
                    this.connectLock.unlock();
                }
            }
        };
        return new RealmsLongConfirmationScreen(booleanConsumer, RealmsLongConfirmationScreen.Type.Info, Component.translatable("mco.configure.world.resourcepack.question.line1"), Component.translatable("mco.configure.world.resourcepack.question.line2"), true);
    }

    private CompletableFuture<?> scheduleResourcePackDownload(RealmsServerAddress realmsServerAddress) {
        try {
            return Minecraft.getInstance().getClientPackSource().downloadAndSelectResourcePack(new URL(realmsServerAddress.resourcePackUrl), realmsServerAddress.resourcePackHash, false);
        } catch (Exception exception) {
            CompletableFuture completableFuture = new CompletableFuture();
            completableFuture.completeExceptionally(exception);
            return completableFuture;
        }
    }
}

