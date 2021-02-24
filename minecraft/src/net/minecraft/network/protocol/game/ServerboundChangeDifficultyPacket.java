package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener> {
	private final Difficulty difficulty;

	public ServerboundChangeDifficultyPacket(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChangeDifficulty(this);
	}

	public ServerboundChangeDifficultyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.difficulty = Difficulty.byId(friendlyByteBuf.readUnsignedByte());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.difficulty.getId());
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
