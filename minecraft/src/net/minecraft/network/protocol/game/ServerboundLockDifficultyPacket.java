package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
	private boolean locked;

	public ServerboundLockDifficultyPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundLockDifficultyPacket(boolean bl) {
		this.locked = bl;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleLockDifficulty(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.locked = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBoolean(this.locked);
	}

	public boolean isLocked() {
		return this.locked;
	}
}
