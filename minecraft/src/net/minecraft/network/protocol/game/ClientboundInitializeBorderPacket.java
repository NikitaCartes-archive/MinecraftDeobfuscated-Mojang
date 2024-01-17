package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundInitializeBorderPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundInitializeBorderPacket> STREAM_CODEC = Packet.codec(
		ClientboundInitializeBorderPacket::write, ClientboundInitializeBorderPacket::new
	);
	private final double newCenterX;
	private final double newCenterZ;
	private final double oldSize;
	private final double newSize;
	private final long lerpTime;
	private final int newAbsoluteMaxSize;
	private final int warningBlocks;
	private final int warningTime;

	private ClientboundInitializeBorderPacket(FriendlyByteBuf friendlyByteBuf) {
		this.newCenterX = friendlyByteBuf.readDouble();
		this.newCenterZ = friendlyByteBuf.readDouble();
		this.oldSize = friendlyByteBuf.readDouble();
		this.newSize = friendlyByteBuf.readDouble();
		this.lerpTime = friendlyByteBuf.readVarLong();
		this.newAbsoluteMaxSize = friendlyByteBuf.readVarInt();
		this.warningBlocks = friendlyByteBuf.readVarInt();
		this.warningTime = friendlyByteBuf.readVarInt();
	}

	public ClientboundInitializeBorderPacket(WorldBorder worldBorder) {
		this.newCenterX = worldBorder.getCenterX();
		this.newCenterZ = worldBorder.getCenterZ();
		this.oldSize = worldBorder.getSize();
		this.newSize = worldBorder.getLerpTarget();
		this.lerpTime = worldBorder.getLerpRemainingTime();
		this.newAbsoluteMaxSize = worldBorder.getAbsoluteMaxSize();
		this.warningBlocks = worldBorder.getWarningBlocks();
		this.warningTime = worldBorder.getWarningTime();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.newCenterX);
		friendlyByteBuf.writeDouble(this.newCenterZ);
		friendlyByteBuf.writeDouble(this.oldSize);
		friendlyByteBuf.writeDouble(this.newSize);
		friendlyByteBuf.writeVarLong(this.lerpTime);
		friendlyByteBuf.writeVarInt(this.newAbsoluteMaxSize);
		friendlyByteBuf.writeVarInt(this.warningBlocks);
		friendlyByteBuf.writeVarInt(this.warningTime);
	}

	@Override
	public PacketType<ClientboundInitializeBorderPacket> type() {
		return GamePacketTypes.CLIENTBOUND_INITIALIZE_BORDER;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleInitializeBorder(this);
	}

	public double getNewCenterX() {
		return this.newCenterX;
	}

	public double getNewCenterZ() {
		return this.newCenterZ;
	}

	public double getNewSize() {
		return this.newSize;
	}

	public double getOldSize() {
		return this.oldSize;
	}

	public long getLerpTime() {
		return this.lerpTime;
	}

	public int getNewAbsoluteMaxSize() {
		return this.newAbsoluteMaxSize;
	}

	public int getWarningTime() {
		return this.warningTime;
	}

	public int getWarningBlocks() {
		return this.warningBlocks;
	}
}
