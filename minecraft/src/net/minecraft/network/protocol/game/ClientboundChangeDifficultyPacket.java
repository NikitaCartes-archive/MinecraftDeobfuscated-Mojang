package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundChangeDifficultyPacket> STREAM_CODEC = Packet.codec(
		ClientboundChangeDifficultyPacket::write, ClientboundChangeDifficultyPacket::new
	);
	private final Difficulty difficulty;
	private final boolean locked;

	public ClientboundChangeDifficultyPacket(Difficulty difficulty, boolean bl) {
		this.difficulty = difficulty;
		this.locked = bl;
	}

	private ClientboundChangeDifficultyPacket(FriendlyByteBuf friendlyByteBuf) {
		this.difficulty = Difficulty.byId(friendlyByteBuf.readUnsignedByte());
		this.locked = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.difficulty.getId());
		friendlyByteBuf.writeBoolean(this.locked);
	}

	@Override
	public PacketType<ClientboundChangeDifficultyPacket> type() {
		return GamePacketTypes.CLIENTBOUND_CHANGE_DIFFICULTY;
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
