/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.dedicated;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.slf4j.Logger;

public class DedicatedPlayerList
extends PlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DedicatedPlayerList(DedicatedServer dedicatedServer, RegistryAccess.RegistryHolder registryHolder, PlayerDataStorage playerDataStorage) {
        super(dedicatedServer, registryHolder, playerDataStorage, dedicatedServer.getProperties().maxPlayers);
        DedicatedServerProperties dedicatedServerProperties = dedicatedServer.getProperties();
        this.setViewDistance(dedicatedServerProperties.viewDistance);
        this.setSimulationDistance(dedicatedServerProperties.simulationDistance);
        super.setUsingWhiteList(dedicatedServerProperties.whiteList.get());
        this.loadUserBanList();
        this.saveUserBanList();
        this.loadIpBanList();
        this.saveIpBanList();
        this.loadOps();
        this.loadWhiteList();
        this.saveOps();
        if (!this.getWhiteList().getFile().exists()) {
            this.saveWhiteList();
        }
    }

    @Override
    public void setUsingWhiteList(boolean bl) {
        super.setUsingWhiteList(bl);
        this.getServer().storeUsingWhiteList(bl);
    }

    @Override
    public void op(GameProfile gameProfile) {
        super.op(gameProfile);
        this.saveOps();
    }

    @Override
    public void deop(GameProfile gameProfile) {
        super.deop(gameProfile);
        this.saveOps();
    }

    @Override
    public void reloadWhiteList() {
        this.loadWhiteList();
    }

    private void saveIpBanList() {
        try {
            this.getIpBans().save();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to save ip banlist: ", iOException);
        }
    }

    private void saveUserBanList() {
        try {
            this.getBans().save();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to save user banlist: ", iOException);
        }
    }

    private void loadIpBanList() {
        try {
            this.getIpBans().load();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load ip banlist: ", iOException);
        }
    }

    private void loadUserBanList() {
        try {
            this.getBans().load();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to load user banlist: ", iOException);
        }
    }

    private void loadOps() {
        try {
            this.getOps().load();
        } catch (Exception exception) {
            LOGGER.warn("Failed to load operators list: ", exception);
        }
    }

    private void saveOps() {
        try {
            this.getOps().save();
        } catch (Exception exception) {
            LOGGER.warn("Failed to save operators list: ", exception);
        }
    }

    private void loadWhiteList() {
        try {
            this.getWhiteList().load();
        } catch (Exception exception) {
            LOGGER.warn("Failed to load white-list: ", exception);
        }
    }

    private void saveWhiteList() {
        try {
            this.getWhiteList().save();
        } catch (Exception exception) {
            LOGGER.warn("Failed to save white-list: ", exception);
        }
    }

    @Override
    public boolean isWhiteListed(GameProfile gameProfile) {
        return !this.isUsingWhitelist() || this.isOp(gameProfile) || this.getWhiteList().isWhiteListed(gameProfile);
    }

    @Override
    public DedicatedServer getServer() {
        return (DedicatedServer)super.getServer();
    }

    @Override
    public boolean canBypassPlayerLimit(GameProfile gameProfile) {
        return this.getOps().canBypassPlayerLimit(gameProfile);
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

