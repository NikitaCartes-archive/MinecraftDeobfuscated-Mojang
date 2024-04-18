package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundGameProfilePacket(GameProfile gameProfile, @Deprecated(forRemoval = true) boolean strictErrorHandling)
	implements Packet<ClientLoginPacketListener> {
	public static final StreamCodec<ByteBuf, ClientboundGameProfilePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.GAME_PROFILE,
		ClientboundGameProfilePacket::gameProfile,
		ByteBufCodecs.BOOL,
		ClientboundGameProfilePacket::strictErrorHandling,
		ClientboundGameProfilePacket::new
	);

	@Override
	public PacketType<ClientboundGameProfilePacket> type() {
		return LoginPacketTypes.CLIENTBOUND_GAME_PROFILE;
	}

	public void handle(ClientLoginPacketListener clientLoginPacketListener) {
		clientLoginPacketListener.handleGameProfile(this);
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
