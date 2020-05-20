/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class ClientboundLoginPacket
implements Packet<ClientGamePacketListener> {
    private int playerId;
    private long seed;
    private boolean hardcore;
    private GameType gameType;
    private RegistryAccess.RegistryHolder registryHolder;
    private ResourceLocation dimension;
    private int maxPlayers;
    private int chunkRadius;
    private boolean reducedDebugInfo;
    private boolean showDeathScreen;
    private boolean isDebug;
    private boolean isFlat;

    public ClientboundLoginPacket() {
    }

    public ClientboundLoginPacket(int i, GameType gameType, long l, boolean bl, RegistryAccess.RegistryHolder registryHolder, ResourceLocation resourceLocation, int j, int k, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        this.playerId = i;
        this.registryHolder = registryHolder;
        this.dimension = resourceLocation;
        this.seed = l;
        this.gameType = gameType;
        this.maxPlayers = j;
        this.hardcore = bl;
        this.chunkRadius = k;
        this.reducedDebugInfo = bl2;
        this.showDeathScreen = bl3;
        this.isDebug = bl4;
        this.isFlat = bl5;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.playerId = friendlyByteBuf.readInt();
        int i = friendlyByteBuf.readUnsignedByte();
        this.hardcore = (i & 8) == 8;
        this.gameType = GameType.byId(i &= 0xFFFFFFF7);
        this.registryHolder = friendlyByteBuf.readWithCodec(RegistryAccess.RegistryHolder.CODEC);
        this.dimension = friendlyByteBuf.readResourceLocation();
        this.seed = friendlyByteBuf.readLong();
        this.maxPlayers = friendlyByteBuf.readUnsignedByte();
        this.chunkRadius = friendlyByteBuf.readVarInt();
        this.reducedDebugInfo = friendlyByteBuf.readBoolean();
        this.showDeathScreen = friendlyByteBuf.readBoolean();
        this.isDebug = friendlyByteBuf.readBoolean();
        this.isFlat = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeInt(this.playerId);
        int i = this.gameType.getId();
        if (this.hardcore) {
            i |= 8;
        }
        friendlyByteBuf.writeByte(i);
        friendlyByteBuf.writeWithCodec(RegistryAccess.RegistryHolder.CODEC, this.registryHolder);
        friendlyByteBuf.writeResourceLocation(this.dimension);
        friendlyByteBuf.writeLong(this.seed);
        friendlyByteBuf.writeByte(this.maxPlayers);
        friendlyByteBuf.writeVarInt(this.chunkRadius);
        friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
        friendlyByteBuf.writeBoolean(this.showDeathScreen);
        friendlyByteBuf.writeBoolean(this.isDebug);
        friendlyByteBuf.writeBoolean(this.isFlat);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleLogin(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @Environment(value=EnvType.CLIENT)
    public long getSeed() {
        return this.seed;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isHardcore() {
        return this.hardcore;
    }

    @Environment(value=EnvType.CLIENT)
    public GameType getGameType() {
        return this.gameType;
    }

    @Environment(value=EnvType.CLIENT)
    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    @Environment(value=EnvType.CLIENT)
    public ResourceLocation getDimension() {
        return this.dimension;
    }

    @Environment(value=EnvType.CLIENT)
    public int getChunkRadius() {
        return this.chunkRadius;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isDebug() {
        return this.isDebug;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isFlat() {
        return this.isFlat;
    }
}

