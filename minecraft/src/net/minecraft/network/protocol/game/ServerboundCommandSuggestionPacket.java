package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
	private int id;
	private String command;

	public ServerboundCommandSuggestionPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundCommandSuggestionPacket(int i, String string) {
		this.id = i;
		this.command = string;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.command = friendlyByteBuf.readUtf(32500);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
