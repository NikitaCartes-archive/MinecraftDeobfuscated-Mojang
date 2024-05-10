package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundProjectilePowerPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundProjectilePowerPacket> STREAM_CODEC = Packet.codec(
		ClientboundProjectilePowerPacket::write, ClientboundProjectilePowerPacket::new
	);
	private final int id;
	private final double accelerationPower;

	public ClientboundProjectilePowerPacket(int i, double d) {
		this.id = i;
		this.accelerationPower = d;
	}

	private ClientboundProjectilePowerPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.accelerationPower = friendlyByteBuf.readDouble();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.accelerationPower);
	}

	@Override
	public PacketType<ClientboundProjectilePowerPacket> type() {
		return GamePacketTypes.CLIENTBOUND_PROJECTILE_POWER;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleProjectilePowerPacket(this);
	}

	public int getId() {
		return this.id;
	}

	public double getAccelerationPower() {
		return this.accelerationPower;
	}
}
