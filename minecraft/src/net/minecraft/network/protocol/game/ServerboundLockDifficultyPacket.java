package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
	private final boolean locked;

	@Environment(EnvType.CLIENT)
	public ServerboundLockDifficultyPacket(boolean bl) {
		this.locked = bl;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleLockDifficulty(this);
	}

	public ServerboundLockDifficultyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.locked = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.locked);
	}

	public boolean isLocked() {
		return this.locked;
	}
}
