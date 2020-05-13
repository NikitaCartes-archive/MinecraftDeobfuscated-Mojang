/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket
implements Packet<ClientGamePacketListener> {
    private DimensionType dimension;
    private long seed;
    private GameType playerGameType;
    private boolean isDebug;
    private boolean isFlat;
    private boolean keepAllPlayerData;

    public ClientboundRespawnPacket() {
    }

    public ClientboundRespawnPacket(DimensionType dimensionType, long l, GameType gameType, boolean bl, boolean bl2, boolean bl3) {
        this.dimension = dimensionType;
        this.seed = l;
        this.playerGameType = gameType;
        this.isDebug = bl;
        this.isFlat = bl2;
        this.keepAllPlayerData = bl3;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRespawn(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.dimension = DimensionType.getById(friendlyByteBuf.readInt());
        this.seed = friendlyByteBuf.readLong();
        this.playerGameType = GameType.byId(friendlyByteBuf.readUnsignedByte());
        this.isDebug = friendlyByteBuf.readBoolean();
        this.isFlat = friendlyByteBuf.readBoolean();
        this.keepAllPlayerData = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeInt(this.dimension.getId());
        friendlyByteBuf.writeLong(this.seed);
        friendlyByteBuf.writeByte(this.playerGameType.getId());
        friendlyByteBuf.writeBoolean(this.isDebug);
        friendlyByteBuf.writeBoolean(this.isFlat);
        friendlyByteBuf.writeBoolean(this.keepAllPlayerData);
    }

    @Environment(value=EnvType.CLIENT)
    public DimensionType getDimension() {
        return this.dimension;
    }

    @Environment(value=EnvType.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @Environment(value=EnvType.CLIENT)
    public GameType getPlayerGameType() {
        return this.playerGameType;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldKeepAllPlayerData() {
        return this.keepAllPlayerData;
    }
}

