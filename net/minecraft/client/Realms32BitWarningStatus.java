/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.Realms32bitWarningScreen;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Realms32BitWarningStatus {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    @Nullable
    private CompletableFuture<Boolean> subscriptionCheck;
    private boolean warningScreenShown;

    public Realms32BitWarningStatus(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void showRealms32BitWarningIfNeeded(Screen screen) {
        if (!this.minecraft.is64Bit() && !this.minecraft.options.skipRealms32bitWarning && !this.warningScreenShown && this.checkForRealmsSubscription().booleanValue()) {
            this.minecraft.setScreen(new Realms32bitWarningScreen(screen));
            this.warningScreenShown = true;
        }
    }

    private Boolean checkForRealmsSubscription() {
        if (this.subscriptionCheck == null) {
            this.subscriptionCheck = CompletableFuture.supplyAsync(this::hasRealmsSubscription, Util.backgroundExecutor());
        }
        try {
            return this.subscriptionCheck.getNow(false);
        } catch (CompletionException completionException) {
            LOGGER.warn("Failed to retrieve realms subscriptions", completionException);
            this.warningScreenShown = true;
            return false;
        }
    }

    private boolean hasRealmsSubscription() {
        try {
            return RealmsClient.create().listWorlds().servers.stream().anyMatch(realmsServer -> realmsServer.ownerUUID != null && !realmsServer.expired && realmsServer.ownerUUID.equals(this.minecraft.getUser().getUuid()));
        } catch (RealmsServiceException realmsServiceException) {
            return false;
        }
    }
}

