/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public record ClientboundLoginPacket(int playerId, boolean hardcore, GameType gameType, @Nullable GameType previousGameType, Set<ResourceKey<Level>> levels, RegistryAccess.Frozen registryHolder, ResourceKey<DimensionType> dimensionType, ResourceKey<Level> dimension, long seed, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation) implements Packet<ClientGamePacketListener>
{
    public ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readInt(), friendlyByteBuf2.readBoolean(), GameType.byId(friendlyByteBuf2.readByte()), GameType.byNullableId(friendlyByteBuf2.readByte()), friendlyByteBuf2.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBuf -> friendlyByteBuf.readResourceKey(Registries.DIMENSION)), friendlyByteBuf2.readWithCodec(RegistrySynchronization.NETWORK_CODEC).freeze(), friendlyByteBuf2.readResourceKey(Registries.DIMENSION_TYPE), friendlyByteBuf2.readResourceKey(Registries.DIMENSION), friendlyByteBuf2.readLong(), friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readBoolean(), friendlyByteBuf2.readOptional(FriendlyByteBuf::readGlobalPos));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.playerId);
        friendlyByteBuf.writeBoolean(this.hardcore);
        friendlyByteBuf.writeByte(this.gameType.getId());
        friendlyByteBuf.writeByte(GameType.getNullableId(this.previousGameType));
        friendlyByteBuf.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
        friendlyByteBuf.writeWithCodec(RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
        friendlyByteBuf.writeResourceKey(this.dimensionType);
        friendlyByteBuf.writeResourceKey(this.dimension);
        friendlyByteBuf.writeLong(this.seed);
        friendlyByteBuf.writeVarInt(this.maxPlayers);
        friendlyByteBuf.writeVarInt(this.chunkRadius);
        friendlyByteBuf.writeVarInt(this.simulationDistance);
        friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
        friendlyByteBuf.writeBoolean(this.showDeathScreen);
        friendlyByteBuf.writeBoolean(this.isDebug);
        friendlyByteBuf.writeBoolean(this.isFlat);
        friendlyByteBuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleLogin(this);
    }

    @Nullable
    public GameType previousGameType() {
        return this.previousGameType;
    }
}

