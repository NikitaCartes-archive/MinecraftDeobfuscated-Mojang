package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
	private GameProfile gameProfile;

	public ClientboundGameProfilePacket() {
	}

	public ClientboundGameProfilePacket(GameProfile gameProfile) {
		this.gameProfile = gameProfile;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		String string = friendlyByteBuf.readUtf(36);
		String string2 = friendlyByteBuf.readUtf(16);
		UUID uUID = UUID.fromString(string);
		this.gameProfile = new GameProfile(uUID, string2);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		UUID uUID = this.gameProfile.getId();
		friendlyByteBuf.writeUtf(uUID == null ? "" : uUID.toString());
		friendlyByteBuf.writeUtf(this.gameProfile.getName());
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleGameProfile(this);
	}

	@Environment(EnvType.CLIENT)
	public GameProfile getGameProfile() {
		return this.gameProfile;
	}
}
