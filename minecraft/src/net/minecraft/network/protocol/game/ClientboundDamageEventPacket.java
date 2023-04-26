package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition)
	implements Packet<ClientGamePacketListener> {
	public ClientboundDamageEventPacket(Entity entity, DamageSource damageSource) {
		this(
			entity.getId(),
			entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getId(damageSource.type()),
			damageSource.getEntity() != null ? damageSource.getEntity().getId() : -1,
			damageSource.getDirectEntity() != null ? damageSource.getDirectEntity().getId() : -1,
			Optional.ofNullable(damageSource.sourcePositionRaw())
		);
	}

	public ClientboundDamageEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			readOptionalEntityId(friendlyByteBuf),
			readOptionalEntityId(friendlyByteBuf),
			friendlyByteBuf.readOptional(friendlyByteBufx -> new Vec3(friendlyByteBufx.readDouble(), friendlyByteBufx.readDouble(), friendlyByteBufx.readDouble()))
		);
	}

	private static void writeOptionalEntityId(FriendlyByteBuf friendlyByteBuf, int i) {
		friendlyByteBuf.writeVarInt(i + 1);
	}

	private static int readOptionalEntityId(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readVarInt() - 1;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeVarInt(this.sourceTypeId);
		writeOptionalEntityId(friendlyByteBuf, this.sourceCauseId);
		writeOptionalEntityId(friendlyByteBuf, this.sourceDirectId);
		friendlyByteBuf.writeOptional(this.sourcePosition, (friendlyByteBufx, vec3) -> {
			friendlyByteBufx.writeDouble(vec3.x());
			friendlyByteBufx.writeDouble(vec3.y());
			friendlyByteBufx.writeDouble(vec3.z());
		});
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDamageEvent(this);
	}

	public DamageSource getSource(Level level) {
		Holder<DamageType> holder = (Holder<DamageType>)level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(this.sourceTypeId).get();
		if (this.sourcePosition.isPresent()) {
			return new DamageSource(holder, (Vec3)this.sourcePosition.get());
		} else {
			Entity entity = level.getEntity(this.sourceCauseId);
			Entity entity2 = level.getEntity(this.sourceDirectId);
			return new DamageSource(holder, entity2, entity);
		}
	}
}
