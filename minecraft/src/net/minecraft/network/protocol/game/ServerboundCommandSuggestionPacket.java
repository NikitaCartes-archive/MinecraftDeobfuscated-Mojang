package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
	private final int id;
	private final String command;

	@Environment(EnvType.CLIENT)
	public ServerboundCommandSuggestionPacket(int i, String string) {
		this.id = i;
		this.command = string;
	}

	public ServerboundCommandSuggestionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.command = friendlyByteBuf.readUtf(32500);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeUtf(this.command, 32500);
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
