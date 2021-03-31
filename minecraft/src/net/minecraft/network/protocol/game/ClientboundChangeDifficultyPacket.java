package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
	private final Difficulty difficulty;
	private final boolean locked;

	public ClientboundChangeDifficultyPacket(Difficulty difficulty, boolean bl) {
		this.difficulty = difficulty;
		this.locked = bl;
	}

	public ClientboundChangeDifficultyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.difficulty = Difficulty.byId(friendlyByteBuf.readUnsignedByte());
		this.locked = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.difficulty.getId());
		friendlyByteBuf.writeBoolean(this.locked);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChangeDifficulty(this);
	}

	public boolean isLocked() {
		return this.locked;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
