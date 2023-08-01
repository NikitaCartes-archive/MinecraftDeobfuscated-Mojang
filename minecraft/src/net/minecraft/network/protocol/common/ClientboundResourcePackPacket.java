package net.minecraft.network.protocol.common;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientCommonPacketListener> {
	public static final int MAX_HASH_LENGTH = 40;
	private final String url;
	private final String hash;
	private final boolean required;
	@Nullable
	private final Component prompt;

	public ClientboundResourcePackPacket(String string, String string2, boolean bl, @Nullable Component component) {
		if (string2.length() > 40) {
			throw new IllegalArgumentException("Hash is too long (max 40, was " + string2.length() + ")");
		} else {
			this.url = string;
			this.hash = string2;
			this.required = bl;
			this.prompt = component;
		}
	}

	public ClientboundResourcePackPacket(FriendlyByteBuf friendlyByteBuf) {
		this.url = friendlyByteBuf.readUtf();
		this.hash = friendlyByteBuf.readUtf(40);
		this.required = friendlyByteBuf.readBoolean();
		this.prompt = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
		friendlyByteBuf.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleResourcePack(this);
	}

	public String getUrl() {
		return this.url;
	}

	public String getHash() {
		return this.hash;
	}

	public boolean isRequired() {
		return this.required;
	}

	@Nullable
	public Component getPrompt() {
		return this.prompt;
	}
}
