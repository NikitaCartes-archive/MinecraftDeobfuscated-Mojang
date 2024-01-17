package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetTitlesAnimationPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetTitlesAnimationPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetTitlesAnimationPacket::write, ClientboundSetTitlesAnimationPacket::new
	);
	private final int fadeIn;
	private final int stay;
	private final int fadeOut;

	public ClientboundSetTitlesAnimationPacket(int i, int j, int k) {
		this.fadeIn = i;
		this.stay = j;
		this.fadeOut = k;
	}

	private ClientboundSetTitlesAnimationPacket(FriendlyByteBuf friendlyByteBuf) {
		this.fadeIn = friendlyByteBuf.readInt();
		this.stay = friendlyByteBuf.readInt();
		this.fadeOut = friendlyByteBuf.readInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.fadeIn);
		friendlyByteBuf.writeInt(this.stay);
		friendlyByteBuf.writeInt(this.fadeOut);
	}

	@Override
	public PacketType<ClientboundSetTitlesAnimationPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_TITLES_ANIMATION;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.setTitlesAnimation(this);
	}

	public int getFadeIn() {
		return this.fadeIn;
	}

	public int getStay() {
		return this.stay;
	}

	public int getFadeOut() {
		return this.fadeOut;
	}
}
