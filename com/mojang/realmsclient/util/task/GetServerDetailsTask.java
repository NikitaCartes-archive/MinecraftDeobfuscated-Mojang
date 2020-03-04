/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import com.mojang.realmsclient.util.task.ConnectTask;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

@Environment(value=EnvType.CLIENT)
public class GetServerDetailsTask
extends LongRunningTask {
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
        this.setTitle(I18n.get("mco.connect.connecting", new Object[0]));
        RealmsClient realmsClient = RealmsClient.create();
        boolean bl2 = false;
        boolean bl22 = false;
        int i = 5;
        RealmsServerAddress realmsServerAddress = null;
        boolean bl3 = false;
        boolean bl4 = false;
        for (int j = 0; j < 40 && !this.aborted(); ++j) {
            try {
                realmsServerAddress = realmsClient.join(this.server.id);
                bl2 = true;
            } catch (RetryCallException retryCallException) {
                i = retryCallException.delaySeconds;
            } catch (RealmsServiceException realmsServiceException) {
                if (realmsServiceException.errorCode == 6002) {
                    bl3 = true;
                    break;
                }
                if (realmsServiceException.errorCode == 6006) {
                    bl4 = true;
                    break;
                }
                bl22 = true;
                this.error(realmsServiceException.toString());
                LOGGER.error("Couldn't connect to world", (Throwable)realmsServiceException);
                break;
            } catch (Exception exception) {
                bl22 = true;
                LOGGER.error("Couldn't connect to world", (Throwable)exception);
                this.error(exception.getLocalizedMessage());
                break;
            }
            if (bl2) break;
            this.sleep(i);
        }
        if (bl3) {
            GetServerDetailsTask.setScreen(new RealmsTermsScreen(this.lastScreen, this.mainScreen, this.server));
        } else if (bl4) {
            if (this.server.ownerUUID.equals(Minecraft.getInstance().getUser().getUuid())) {
                RealmsBrokenWorldScreen realmsBrokenWorldScreen = new RealmsBrokenWorldScreen(this.lastScreen, this.mainScreen, this.server.id);
                if (this.server.worldType == RealmsServer.WorldType.MINIGAME) {
                    realmsBrokenWorldScreen.setTitle(I18n.get("mco.brokenworld.minigame.title", new Object[0]));
                }
                GetServerDetailsTask.setScreen(realmsBrokenWorldScreen);
            } else {
                GetServerDetailsTask.setScreen(new RealmsGenericErrorScreen(I18n.get("mco.brokenworld.nonowner.title", new Object[0]), I18n.get("mco.brokenworld.nonowner.error", new Object[0]), this.lastScreen));
            }
        } else if (!this.aborted() && !bl22) {
            if (bl2) {
                RealmsServerAddress realmsServerAddress2 = realmsServerAddress;
                if (realmsServerAddress2.resourcePackUrl != null && realmsServerAddress2.resourcePackHash != null) {
                    String string = I18n.get("mco.configure.world.resourcepack.question.line1", new Object[0]);
                    String string2 = I18n.get("mco.configure.world.resourcepack.question.line2", new Object[0]);
                    GetServerDetailsTask.setScreen(new RealmsLongConfirmationScreen(bl -> {
                        try {
                            if (bl) {
                                Function<Throwable, Void> function = throwable -> {
                                    Minecraft.getInstance().getClientPackSource().clearServerPack();
                                    LOGGER.error(throwable);
                                    GetServerDetailsTask.setScreen(new RealmsGenericErrorScreen("Failed to download resource pack!", this.lastScreen));
                                    return null;
                                };
                                try {
                                    ((CompletableFuture)Minecraft.getInstance().getClientPackSource().downloadAndSelectResourcePack(realmsServerAddress.resourcePackUrl, realmsServerAddress.resourcePackHash).thenRun(() -> this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, realmsServerAddress2))))).exceptionally(function);
                                } catch (Exception exception) {
                                    function.apply(exception);
                                }
                            } else {
                                GetServerDetailsTask.setScreen(this.lastScreen);
                            }
                        } finally {
                            if (this.connectLock != null && this.connectLock.isHeldByCurrentThread()) {
                                this.connectLock.unlock();
                            }
                        }
                    }, RealmsLongConfirmationScreen.Type.Info, string, string2, true));
                } else {
                    this.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new ConnectTask(this.lastScreen, realmsServerAddress2)));
                }
            } else {
                this.error(I18n.get("mco.errorMessage.connectionFailure", new Object[0]));
            }
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException interruptedException) {
            LOGGER.warn(interruptedException.getLocalizedMessage());
        }
    }
}

