package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderPacket implements Packet<ClientGamePacketListener> {
	private ClientboundSetBorderPacket.Type type;
	private int newAbsoluteMaxSize;
	private double newCenterX;
	private double newCenterZ;
	private double newSize;
	private double oldSize;
	private long lerpTime;
	private int warningTime;
	private int warningBlocks;

	public ClientboundSetBorderPacket() {
	}

	public ClientboundSetBorderPacket(WorldBorder worldBorder, ClientboundSetBorderPacket.Type type) {
		this.type = type;
		this.newCenterX = worldBorder.getCenterX();
		this.newCenterZ = worldBorder.getCenterZ();
		this.oldSize = worldBorder.getSize();
		this.newSize = worldBorder.getLerpTarget();
		this.lerpTime = worldBorder.getLerpRemainingTime();
		this.newAbsoluteMaxSize = worldBorder.getAbsoluteMaxSize();
		this.warningBlocks = worldBorder.getWarningBlocks();
		this.warningTime = worldBorder.getWarningTime();
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.type = friendlyByteBuf.readEnum(ClientboundSetBorderPacket.Type.class);
		switch (this.type) {
			case SET_SIZE:
				this.newSize = friendlyByteBuf.readDouble();
				break;
			case LERP_SIZE:
				this.oldSize = friendlyByteBuf.readDouble();
				this.newSize = friendlyByteBuf.readDouble();
				this.lerpTime = friendlyByteBuf.readVarLong();
				break;
			case SET_CENTER:
				this.newCenterX = friendlyByteBuf.readDouble();
				this.newCenterZ = friendlyByteBuf.readDouble();
				break;
			case SET_WARNING_BLOCKS:
				this.warningBlocks = friendlyByteBuf.readVarInt();
				break;
			case SET_WARNING_TIME:
				this.warningTime = friendlyByteBuf.readVarInt();
				break;
			case INITIALIZE:
				this.newCenterX = friendlyByteBuf.readDouble();
				this.newCenterZ = friendlyByteBuf.readDouble();
				this.oldSize = friendlyByteBuf.readDouble();
				this.newSize = friendlyByteBuf.readDouble();
				this.lerpTime = friendlyByteBuf.readVarLong();
				this.newAbsoluteMaxSize = friendlyByteBuf.readVarInt();
				this.warningBlocks = friendlyByteBuf.readVarInt();
				this.warningTime = friendlyByteBuf.readVarInt();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.type);
		switch (this.type) {
			case SET_SIZE:
				friendlyByteBuf.writeDouble(this.newSize);
				break;
			case LERP_SIZE:
				friendlyByteBuf.writeDouble(this.oldSize);
				friendlyByteBuf.writeDouble(this.newSize);
				friendlyByteBuf.writeVarLong(this.lerpTime);
				break;
			case SET_CENTER:
				friendlyByteBuf.writeDouble(this.newCenterX);
				friendlyByteBuf.writeDouble(this.newCenterZ);
				break;
			case SET_WARNING_BLOCKS:
				friendlyByteBuf.writeVarInt(this.warningBlocks);
				break;
			case SET_WARNING_TIME:
				friendlyByteBuf.writeVarInt(this.warningTime);
				break;
			case INITIALIZE:
				friendlyByteBuf.writeDouble(this.newCenterX);
				friendlyByteBuf.writeDouble(this.newCenterZ);
				friendlyByteBuf.writeDouble(this.oldSize);
				friendlyByteBuf.writeDouble(this.newSize);
				friendlyByteBuf.writeVarLong(this.lerpTime);
				friendlyByteBuf.writeVarInt(this.newAbsoluteMaxSize);
				friendlyByteBuf.writeVarInt(this.warningBlocks);
				friendlyByteBuf.writeVarInt(this.warningTime);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorder(this);
	}

	@Environment(EnvType.CLIENT)
	public void applyChanges(WorldBorder worldBorder) {
		switch (this.type) {
			case SET_SIZE:
				worldBorder.setSize(this.newSize);
				break;
			case LERP_SIZE:
				worldBorder.lerpSizeBetween(this.oldSize, this.newSize, this.lerpTime);
				break;
			case SET_CENTER:
				worldBorder.setCenter(this.newCenterX, this.newCenterZ);
				break;
			case SET_WARNING_BLOCKS:
				worldBorder.setWarningBlocks(this.warningBlocks);
				break;
			case SET_WARNING_TIME:
				worldBorder.setWarningTime(this.warningTime);
				break;
			case INITIALIZE:
				worldBorder.setCenter(this.newCenterX, this.newCenterZ);
				if (this.lerpTime > 0L) {
					worldBorder.lerpSizeBetween(this.oldSize, this.newSize, this.lerpTime);
				} else {
					worldBorder.setSize(this.newSize);
				}

				worldBorder.setAbsoluteMaxSize(this.newAbsoluteMaxSize);
				worldBorder.setWarningBlocks(this.warningBlocks);
				worldBorder.setWarningTime(this.warningTime);
		}
	}

	public static enum Type {
		SET_SIZE,
		LERP_SIZE,
		SET_CENTER,
		INITIALIZE,
		SET_WARNING_TIME,
		SET_WARNING_BLOCKS;
	}
}
