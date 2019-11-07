/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.mojang.bridge.game.GameSession;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;

@Environment(value=EnvType.CLIENT)
public class Session
implements GameSession {
    private final int players;
    private final boolean isRemoteServer;
    private final String difficulty;
    private final String gameMode;
    private final UUID id;

    public Session(ClientLevel clientLevel, LocalPlayer localPlayer, ClientPacketListener clientPacketListener) {
        this.players = clientPacketListener.getOnlinePlayers().size();
        this.isRemoteServer = !clientPacketListener.getConnection().isMemoryConnection();
        this.difficulty = clientLevel.getDifficulty().getKey();
        PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(localPlayer.getUUID());
        this.gameMode = playerInfo != null ? playerInfo.getGameMode().getName() : "unknown";
        this.id = clientPacketListener.getId();
    }

    @Override
    public int getPlayerCount() {
        return this.players;
    }

    @Override
    public boolean isRemoteServer() {
        return this.isRemoteServer;
    }

    @Override
    public String getDifficulty() {
        return this.difficulty;
    }

    @Override
    public String getGameMode() {
        return this.gameMode;
    }

    @Override
    public UUID getSessionId() {
        return this.id;
    }
}

