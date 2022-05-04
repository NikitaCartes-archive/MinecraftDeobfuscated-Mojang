package net.minecraft.network.protocol.login;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ServerboundHelloPacket(String name, Optional<ProfilePublicKey.Data> publicKey) implements Packet<ServerLoginPacketListener> {
	public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(16), friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readWithCodec(ProfilePublicKey.Data.CODEC)));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.name, 16);
		friendlyByteBuf.writeOptional(this.publicKey, (friendlyByteBufx, data) -> friendlyByteBufx.writeWithCodec(ProfilePublicKey.Data.CODEC, data));
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleHello(this);
	}
}
