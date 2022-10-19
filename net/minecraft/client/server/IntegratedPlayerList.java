/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Environment(value=EnvType.CLIENT)
public class IntegratedPlayerList
extends PlayerList {
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer integratedServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage) {
        super(integratedServer, layeredRegistryAccess, playerDataStorage, 8);
        this.setViewDistance(10);
    }

    @Override
    protected void save(ServerPlayer serverPlayer) {
        if (this.getServer().isSingleplayerOwner(serverPlayer.getGameProfile())) {
            this.playerData = serverPlayer.saveWithoutId(new CompoundTag());
        }
        super.save(serverPlayer);
    }

    @Override
    public Component canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile) {
        if (this.getServer().isSingleplayerOwner(gameProfile) && this.getPlayerByName(gameProfile.getName()) != null) {
            return Component.translatable("multiplayer.disconnect.name_taken");
        }
        return super.canPlayerLogin(socketAddress, gameProfile);
    }

    @Override
    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }

    @Override
    public CompoundTag getSingleplayerData() {
        return this.playerData;
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

