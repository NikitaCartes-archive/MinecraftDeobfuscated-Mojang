/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsNotificationsScreen
extends RealmsScreen {
    private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
    private volatile int numberOfPendingInvites;
    private static boolean checkedMcoAvailability;
    private static boolean trialAvailable;
    private static boolean validClient;
    private static boolean hasUnreadNews;
    private static final List<RealmsDataFetcher.Task> tasks;

    public RealmsNotificationsScreen(RealmsScreen realmsScreen) {
    }

    @Override
    public void init() {
        this.checkIfMcoEnabled();
        this.setKeyboardHandlerSendRepeatsToGui(true);
    }

    @Override
    public void tick() {
        if (!(Realms.getRealmsNotificationsEnabled() && Realms.inTitleScreen() && validClient || realmsDataFetcher.isStopped())) {
            realmsDataFetcher.stop();
            return;
        }
        if (!validClient || !Realms.getRealmsNotificationsEnabled()) {
            return;
        }
        realmsDataFetcher.initWithSpecificTaskList(tasks);
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
            this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
        }
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE)) {
            trialAvailable = realmsDataFetcher.isTrialAvailable();
        }
        if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
            hasUnreadNews = realmsDataFetcher.hasUnreadNews();
        }
        realmsDataFetcher.markClean();
    }

    private void checkIfMcoEnabled() {
        if (!checkedMcoAvailability) {
            checkedMcoAvailability = true;
            new Thread("Realms Notification Availability checker #1"){

                @Override
                public void run() {
                    RealmsClient realmsClient = RealmsClient.createRealmsClient();
                    try {
                        RealmsClient.CompatibleVersionResponse compatibleVersionResponse = realmsClient.clientCompatible();
                        if (!compatibleVersionResponse.equals((Object)RealmsClient.CompatibleVersionResponse.COMPATIBLE)) {
                            return;
                        }
                    } catch (RealmsServiceException realmsServiceException) {
                        if (realmsServiceException.httpResultCode != 401) {
                            checkedMcoAvailability = false;
                        }
                        return;
                    } catch (IOException iOException) {
                        checkedMcoAvailability = false;
                        return;
                    }
                    validClient = true;
                }
            }.start();
        }
    }

    @Override
    public void render(int i, int j, float f) {
        if (validClient) {
            this.drawIcons(i, j);
        }
        super.render(i, j, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return super.mouseClicked(d, e, i);
    }

    private void drawIcons(int i, int j) {
        int k = this.numberOfPendingInvites;
        int l = 24;
        int m = this.height() / 4 + 48;
        int n = this.width() / 2 + 80;
        int o = m + 48 + 2;
        int p = 0;
        if (hasUnreadNews) {
            RealmsScreen.bind("realms:textures/gui/realms/news_notification_mainscreen.png");
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.4f, 0.4f, 0.4f);
            RealmsScreen.blit((int)((double)(n + 2 - p) * 2.5), (int)((double)o * 2.5), 0.0f, 0.0f, 40, 40, 40, 40);
            RenderSystem.popMatrix();
            p += 14;
        }
        if (k != 0) {
            RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.pushMatrix();
            RealmsScreen.blit(n - p, o - 6, 0.0f, 0.0f, 15, 25, 31, 25);
            RenderSystem.popMatrix();
            p += 16;
        }
        if (trialAvailable) {
            RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.pushMatrix();
            int q = 0;
            if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
                q = 8;
            }
            RealmsScreen.blit(n + 4 - p, o + 4, 0.0f, q, 8, 8, 8, 16);
            RenderSystem.popMatrix();
        }
    }

    @Override
    public void removed() {
        realmsDataFetcher.stop();
    }

    static {
        tasks = Arrays.asList(RealmsDataFetcher.Task.PENDING_INVITE, RealmsDataFetcher.Task.TRIAL_AVAILABLE, RealmsDataFetcher.Task.UNREAD_NEWS);
    }
}

