package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
	private final GameProfile gameProfile;

	public ClientboundGameProfilePacket(GameProfile gameProfile) {
		this.gameProfile = gameProfile;
	}

	public ClientboundGameProfilePacket(FriendlyByteBuf friendlyByteBuf) {
		this.gameProfile = friendlyByteBuf.readGameProfile();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeGameProfile(this.gameProfile);
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleGameProfile(this);
	}

	public GameProfile getGameProfile() {
		return this.gameProfile;
	}
}
