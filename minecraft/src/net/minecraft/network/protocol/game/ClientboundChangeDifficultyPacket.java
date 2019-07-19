package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
	private Difficulty difficulty;
	private boolean locked;

	public ClientboundChangeDifficultyPacket() {
	}

	public ClientboundChangeDifficultyPacket(Difficulty difficulty, boolean bl) {
		this.difficulty = difficulty;
		this.locked = bl;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChangeDifficulty(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.difficulty = Difficulty.byId(friendlyByteBuf.readUnsignedByte());
		this.locked = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.difficulty.getId());
		friendlyByteBuf.writeBoolean(this.locked);
	}

	@Environment(EnvType.CLIENT)
	public boolean isLocked() {
		return this.locked;
	}

	@Environment(EnvType.CLIENT)
	public Difficulty getDifficulty() {
		return this.difficulty;
	}
}
