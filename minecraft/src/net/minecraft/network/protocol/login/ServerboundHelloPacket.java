package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundHelloPacket implements Packet<ServerLoginPacketListener> {
	private final GameProfile gameProfile;

	public ServerboundHelloPacket(GameProfile gameProfile) {
		this.gameProfile = gameProfile;
	}

	public ServerboundHelloPacket(FriendlyByteBuf friendlyByteBuf) {
		this.gameProfile = new GameProfile(null, friendlyByteBuf.readUtf(16));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.gameProfile.getName());
	}

	public void handle(ServerLoginPacketListener serverLoginPacketListener) {
		serverLoginPacketListener.handleHello(this);
	}

	public GameProfile getGameProfile() {
		return this.gameProfile;
	}
}
