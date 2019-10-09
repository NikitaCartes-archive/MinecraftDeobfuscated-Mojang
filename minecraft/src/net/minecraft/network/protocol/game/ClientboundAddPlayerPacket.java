package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
	private int entityId;
	private UUID playerId;
	private double x;
	private double y;
	private double z;
	private byte yRot;
	private byte xRot;

	public ClientboundAddPlayerPacket() {
	}

	public ClientboundAddPlayerPacket(Player player) {
		this.entityId = player.getId();
		this.playerId = player.getGameProfile().getId();
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
		this.yRot = (byte)((int)(player.yRot * 256.0F / 360.0F));
		this.xRot = (byte)((int)(player.xRot * 256.0F / 360.0F));
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entityId = friendlyByteBuf.readVarInt();
		this.playerId = friendlyByteBuf.readUUID();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readByte();
		this.xRot = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeUUID(this.playerId);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeByte(this.yRot);
		friendlyByteBuf.writeByte(this.xRot);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddPlayer(this);
	}

	@Environment(EnvType.CLIENT)
	public int getEntityId() {
		return this.entityId;
	}

	@Environment(EnvType.CLIENT)
	public UUID getPlayerId() {
		return this.playerId;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return this.y;
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public byte getyRot() {
		return this.yRot;
	}

	@Environment(EnvType.CLIENT)
	public byte getxRot() {
		return this.xRot;
	}
}
