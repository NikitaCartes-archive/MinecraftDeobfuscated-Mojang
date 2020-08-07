package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.SerializableUUID;
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
		int[] is = new int[4];

		for (int i = 0; i < is.length; i++) {
			is[i] = friendlyByteBuf.readInt();
		}

		UUID uUID = SerializableUUID.uuidFromIntArray(is);
		String string = friendlyByteBuf.readUtf(16);
		this.gameProfile = new GameProfile(uUID, string);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		for (int i : SerializableUUID.uuidToIntArray(this.gameProfile.getId())) {
			friendlyByteBuf.writeInt(i);
		}

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
