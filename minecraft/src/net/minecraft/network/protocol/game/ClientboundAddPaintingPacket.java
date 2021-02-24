package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;

public class ClientboundAddPaintingPacket implements Packet<ClientGamePacketListener> {
	private final int id;
	private final UUID uuid;
	private final BlockPos pos;
	private final Direction direction;
	private final int motive;

	public ClientboundAddPaintingPacket(Painting painting) {
		this.id = painting.getId();
		this.uuid = painting.getUUID();
		this.pos = painting.getPos();
		this.direction = painting.getDirection();
		this.motive = Registry.MOTIVE.getId(painting.motive);
	}

	public ClientboundAddPaintingPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.uuid = friendlyByteBuf.readUUID();
		this.motive = friendlyByteBuf.readVarInt();
		this.pos = friendlyByteBuf.readBlockPos();
		this.direction = Direction.from2DDataValue(friendlyByteBuf.readUnsignedByte());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUUID(this.uuid);
		friendlyByteBuf.writeVarInt(this.motive);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte(this.direction.get2DDataValue());
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddPainting(this);
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public UUID getUUID() {
		return this.uuid;
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}

	@Environment(EnvType.CLIENT)
	public Direction getDirection() {
		return this.direction;
	}

	@Environment(EnvType.CLIENT)
	public Motive getMotive() {
		return Registry.MOTIVE.byId(this.motive);
	}
}
