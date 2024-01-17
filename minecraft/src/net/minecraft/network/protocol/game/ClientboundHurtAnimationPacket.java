package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundHurtAnimationPacket> STREAM_CODEC = Packet.codec(
		ClientboundHurtAnimationPacket::write, ClientboundHurtAnimationPacket::new
	);

	public ClientboundHurtAnimationPacket(LivingEntity livingEntity) {
		this(livingEntity.getId(), livingEntity.getHurtDir());
	}

	private ClientboundHurtAnimationPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFloat());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeFloat(this.yaw);
	}

	@Override
	public PacketType<ClientboundHurtAnimationPacket> type() {
		return GamePacketTypes.CLIENTBOUND_HURT_ANIMATION;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleHurtAnimation(this);
	}
}
