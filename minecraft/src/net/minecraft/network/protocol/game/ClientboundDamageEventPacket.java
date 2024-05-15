package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, Holder<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition)
	implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDamageEventPacket> STREAM_CODEC = Packet.codec(
		ClientboundDamageEventPacket::write, ClientboundDamageEventPacket::new
	);

	public ClientboundDamageEventPacket(Entity entity, DamageSource damageSource) {
		this(
			entity.getId(),
			damageSource.typeHolder(),
			damageSource.getEntity() != null ? damageSource.getEntity().getId() : -1,
			damageSource.getDirectEntity() != null ? damageSource.getDirectEntity().getId() : -1,
			Optional.ofNullable(damageSource.sourcePositionRaw())
		);
	}

	private ClientboundDamageEventPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this(
			registryFriendlyByteBuf.readVarInt(),
			DamageType.STREAM_CODEC.decode(registryFriendlyByteBuf),
			readOptionalEntityId(registryFriendlyByteBuf),
			readOptionalEntityId(registryFriendlyByteBuf),
			registryFriendlyByteBuf.readOptional(friendlyByteBuf -> new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()))
		);
	}

	private static void writeOptionalEntityId(FriendlyByteBuf friendlyByteBuf, int i) {
		friendlyByteBuf.writeVarInt(i + 1);
	}

	private static int readOptionalEntityId(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readVarInt() - 1;
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeVarInt(this.entityId);
		DamageType.STREAM_CODEC.encode(registryFriendlyByteBuf, this.sourceType);
		writeOptionalEntityId(registryFriendlyByteBuf, this.sourceCauseId);
		writeOptionalEntityId(registryFriendlyByteBuf, this.sourceDirectId);
		registryFriendlyByteBuf.writeOptional(this.sourcePosition, (friendlyByteBuf, vec3) -> {
			friendlyByteBuf.writeDouble(vec3.x());
			friendlyByteBuf.writeDouble(vec3.y());
			friendlyByteBuf.writeDouble(vec3.z());
		});
	}

	@Override
	public PacketType<ClientboundDamageEventPacket> type() {
		return GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDamageEvent(this);
	}

	public DamageSource getSource(Level level) {
		if (this.sourcePosition.isPresent()) {
			return new DamageSource(this.sourceType, (Vec3)this.sourcePosition.get());
		} else {
			Entity entity = level.getEntity(this.sourceCauseId);
			Entity entity2 = level.getEntity(this.sourceDirectId);
			return new DamageSource(this.sourceType, entity2, entity);
		}
	}
}
