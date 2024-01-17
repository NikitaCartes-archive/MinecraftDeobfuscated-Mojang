package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundCommandSuggestionPacket> STREAM_CODEC = Packet.codec(
		ServerboundCommandSuggestionPacket::write, ServerboundCommandSuggestionPacket::new
	);
	private final int id;
	private final String command;

	public ServerboundCommandSuggestionPacket(int i, String string) {
		this.id = i;
		this.command = string;
	}

	private ServerboundCommandSuggestionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.command = friendlyByteBuf.readUtf(32500);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUtf(this.command, 32500);
	}

	@Override
	public PacketType<ServerboundCommandSuggestionPacket> type() {
		return GamePacketTypes.SERVERBOUND_COMMAND_SUGGESTION;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleCustomCommandSuggestions(this);
	}

	public int getId() {
		return this.id;
	}

	public String getCommand() {
		return this.command;
	}
}
