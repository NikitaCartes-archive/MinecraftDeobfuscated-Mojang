/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class ClientboundLoginPacket
implements Packet<ClientGamePacketListener> {
    private final int playerId;
    private final long seed;
    private final boolean hardcore;
    private final GameType gameType;
    @Nullable
    private final GameType previousGameType;
    private final Set<ResourceKey<Level>> levels;
    private final RegistryAccess.RegistryHolder registryHolder;
    private final DimensionType dimensionType;
    private final ResourceKey<Level> dimension;
    private final int maxPlayers;
    private final int chunkRadius;
    private final boolean reducedDebugInfo;
    private final boolean showDeathScreen;
    private final boolean isDebug;
    private final boolean isFlat;

    public ClientboundLoginPacket(int i, GameType gameType, @Nullable GameType gameType2, long l, boolean bl, Set<ResourceKey<Level>> set, RegistryAccess.RegistryHolder registryHolder, DimensionType dimensionType, ResourceKey<Level> resourceKey, int j, int k, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        this.playerId = i;
        this.levels = set;
        this.registryHolder = registryHolder;
        this.dimensionType = dimensionType;
        this.dimension = resourceKey;
        this.seed = l;
        this.gameType = gameType;
        this.previousGameType = gameType2;
        this.maxPlayers = j;
        this.hardcore = bl;
        this.chunkRadius = k;
        this.reducedDebugInfo = bl2;
        this.showDeathScreen = bl3;
        this.isDebug = bl4;
        this.isFlat = bl5;
    }

    public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.playerId = friendlyByteBuf2.readInt();
        this.hardcore = friendlyByteBuf2.readBoolean();
        this.gameType = GameType.byId(friendlyByteBuf2.readByte());
        this.previousGameType = GameType.byNullableId(friendlyByteBuf2.readByte());
        this.levels = friendlyByteBuf2.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBuf -> ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation()));
        this.registryHolder = friendlyByteBuf2.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC);
        this.dimensionType = friendlyByteBuf2.readWithCodec(DimensionType.CODEC).get();
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf2.readResourceLocation());
        this.seed = friendlyByteBuf2.readLong();
        this.maxPlayers = friendlyByteBuf2.readVarInt();
        this.chunkRadius = friendlyByteBuf2.readVarInt();
        this.reducedDebugInfo = friendlyByteBuf2.readBoolean();
        this.showDeathScreen = friendlyByteBuf2.readBoolean();
        this.isDebug = friendlyByteBuf2.readBoolean();
        this.isFlat = friendlyByteBuf2.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeInt(this.playerId);
        friendlyByteBuf2.writeBoolean(this.hardcore);
        friendlyByteBuf2.writeByte(this.gameType.getId());
        friendlyByteBuf2.writeByte(GameType.getNullableId(this.previousGameType));
        friendlyByteBuf2.writeCollection(this.levels, (friendlyByteBuf, resourceKey) -> friendlyByteBuf.writeResourceLocation(resourceKey.location()));
        friendlyByteBuf2.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, this.registryHolder);
        friendlyByteBuf2.writeWithCodec(DimensionType.CODEC, () -> this.dimensionType);
        friendlyByteBuf2.writeResourceLocation(this.dimension.location());
        friendlyByteBuf2.writeLong(this.seed);
        friendlyByteBuf2.writeVarInt(this.maxPlayers);
        friendlyByteBuf2.writeVarInt(this.chunkRadius);
        friendlyByteBuf2.writeBoolean(this.reducedDebugInfo);
        friendlyByteBuf2.writeBoolean(this.showDeathScreen);
        friendlyByteBuf2.writeBoolean(this.isDebug);
        friendlyByteBuf2.writeBoolean(this.isFlat);
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

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public GameType getPreviousGameType() {
        return this.previousGameType;
    }

    @Environment(value=EnvType.CLIENT)
    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    @Environment(value=EnvType.CLIENT)
    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    @Environment(value=EnvType.CLIENT)
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Environment(value=EnvType.CLIENT)
    public ResourceKey<Level> getDimension() {
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

