package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderSizePacket implements Packet<ClientGamePacketListener> {
	private final double size;

	public ClientboundSetBorderSizePacket(WorldBorder worldBorder) {
		this.size = worldBorder.getLerpTarget();
	}

	public ClientboundSetBorderSizePacket(FriendlyByteBuf friendlyByteBuf) {
		this.size = friendlyByteBuf.readDouble();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.size);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetBorderSize(this);
	}

	@Environment(EnvType.CLIENT)
	public double getSize() {
		return this.size;
	}
}
