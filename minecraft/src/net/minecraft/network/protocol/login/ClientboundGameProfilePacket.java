package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundGameProfilePacket> STREAM_CODEC = Packet.codec(
		ClientboundGameProfilePacket::write, ClientboundGameProfilePacket::new
	);
	private final GameProfile gameProfile;

	public ClientboundGameProfilePacket(GameProfile gameProfile) {
		this.gameProfile = gameProfile;
	}

	private ClientboundGameProfilePacket(FriendlyByteBuf friendlyByteBuf) {
		this.gameProfile = friendlyByteBuf.readGameProfile();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeGameProfile(this.gameProfile);
	}

	@Override
	public PacketType<ClientboundGameProfilePacket> type() {
		return LoginPacketTypes.CLIENTBOUND_GAME_PROFILE;
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleGameProfile(this);
	}

	public GameProfile getGameProfile() {
		return this.gameProfile;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
