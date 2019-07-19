package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ServerboundTeleportToEntityPacket implements Packet<ServerGamePacketListener> {
	private UUID uuid;

	public ServerboundTeleportToEntityPacket() {
	}

	public ServerboundTeleportToEntityPacket(UUID uUID) {
		this.uuid = uUID;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.uuid = friendlyByteBuf.readUUID();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUUID(this.uuid);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleTeleportToEntityPacket(this);
	}

	@Nullable
	public Entity getEntity(ServerLevel serverLevel) {
		return serverLevel.getEntity(this.uuid);
	}
}
