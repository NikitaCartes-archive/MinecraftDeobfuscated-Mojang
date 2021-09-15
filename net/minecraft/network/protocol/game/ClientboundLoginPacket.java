/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
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

public record ClientboundLoginPacket(int playerId, boolean hardcore, GameType gameType, @Nullable GameType previousGameType, Set<ResourceKey<Level>> levels, RegistryAccess.RegistryHolder registryHolder, DimensionType dimensionType, ResourceKey<Level> dimension, long seed, int maxPlayers, int chunkRadius, boolean reducedDebugInfo, boolean showDeathScreen, boolean isDebug, boolean isFlat) implements Packet
{
    public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readInt(), friendlyByteBuf2.readBoolean(), GameType.byId(friendlyByteBuf2.readByte()), GameType.byNullableId(friendlyByteBuf2.readByte()), friendlyByteBuf2.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBuf -> ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf.readResourceLocation())), friendlyByteBuf2.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC), friendlyByteBuf2.readWithCodec(DimensionType.CODEC).get(), ResourceKey.create(Registry.DIMENSION_REGISTRY, friendlyByteBuf2.readResourceLocation()), friendlyByteBuf2.readLong(), friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean());
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

    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleLogin(this);
    }

    @Nullable
    public GameType previousGameType() {
        return this.previousGameType;
    }
}

