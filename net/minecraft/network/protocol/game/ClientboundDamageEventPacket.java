/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition) implements Packet<ClientGamePacketListener>
{
    public ClientboundDamageEventPacket(Entity entity, DamageSource damageSource) {
        this(entity.getId(), entity.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getId(damageSource.type()), damageSource.getEntity() != null ? damageSource.getEntity().getId() : -1, damageSource.getDirectEntity() != null ? damageSource.getDirectEntity().getId() : -1, Optional.ofNullable(damageSource.sourcePositionRaw()));
    }

    public ClientboundDamageEventPacket(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readVarInt(), friendlyByteBuf2.readVarInt(), ClientboundDamageEventPacket.readOptionalEntityId(friendlyByteBuf2), ClientboundDamageEventPacket.readOptionalEntityId(friendlyByteBuf2), friendlyByteBuf2.readOptional(friendlyByteBuf -> new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble())));
    }

    private static void writeOptionalEntityId(FriendlyByteBuf friendlyByteBuf, int i) {
        friendlyByteBuf.writeVarInt(i + 1);
    }

    private static int readOptionalEntityId(FriendlyByteBuf friendlyByteBuf) {
        return friendlyByteBuf.readVarInt() - 1;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.entityId);
        friendlyByteBuf2.writeVarInt(this.sourceTypeId);
        ClientboundDamageEventPacket.writeOptionalEntityId(friendlyByteBuf2, this.sourceCauseId);
        ClientboundDamageEventPacket.writeOptionalEntityId(friendlyByteBuf2, this.sourceDirectId);
        friendlyByteBuf2.writeOptional(this.sourcePosition, (friendlyByteBuf, vec3) -> {
            friendlyByteBuf.writeDouble(vec3.x());
            friendlyByteBuf.writeDouble(vec3.y());
            friendlyByteBuf.writeDouble(vec3.z());
        });
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleDamageEvent(this);
    }

    public DamageSource getSource(Level level) {
        Holder holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(this.sourceTypeId).get();
        if (this.sourcePosition.isPresent()) {
            return new DamageSource((Holder<DamageType>)holder, this.sourcePosition.get());
        }
        Entity entity = level.getEntity(this.sourceCauseId);
        Entity entity2 = level.getEntity(this.sourceDirectId);
        return new DamageSource(holder, entity2, entity);
    }
}

