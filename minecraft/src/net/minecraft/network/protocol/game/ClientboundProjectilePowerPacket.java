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
	private final double xPower;
	private final double yPower;
	private final double zPower;

	public ClientboundProjectilePowerPacket(int i, double d, double e, double f) {
		this.id = i;
		this.xPower = d;
		this.yPower = e;
		this.zPower = f;
	}

	private ClientboundProjectilePowerPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.xPower = friendlyByteBuf.readDouble();
		this.yPower = friendlyByteBuf.readDouble();
		this.zPower = friendlyByteBuf.readDouble();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeDouble(this.xPower);
		friendlyByteBuf.writeDouble(this.yPower);
		friendlyByteBuf.writeDouble(this.zPower);
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

	public double getXPower() {
		return this.xPower;
	}

	public double getYPower() {
		return this.yPower;
	}

	public double getZPower() {
		return this.zPower;
	}
}
