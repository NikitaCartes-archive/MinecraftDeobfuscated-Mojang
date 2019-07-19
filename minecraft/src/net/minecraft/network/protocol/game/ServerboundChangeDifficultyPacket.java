package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener> {
	private Difficulty difficulty;

	public ServerboundChangeDifficultyPacket() {
	}

	public ServerboundChangeDifficultyPacket(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChangeDifficulty(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.difficulty = Difficulty.byId(friendlyByteBuf.readUnsignedByte());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.difficulty.getId());
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
