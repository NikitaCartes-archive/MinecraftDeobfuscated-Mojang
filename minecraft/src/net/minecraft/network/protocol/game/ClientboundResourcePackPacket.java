package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
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
		if (friendlyByteBuf.readBoolean()) {
			this.prompt = friendlyByteBuf.readComponent();
		} else {
			this.prompt = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.url);
		friendlyByteBuf.writeUtf(this.hash);
		friendlyByteBuf.writeBoolean(this.required);
		if (this.prompt != null) {
			friendlyByteBuf.writeBoolean(true);
			friendlyByteBuf.writeComponent(this.prompt);
		} else {
			friendlyByteBuf.writeBoolean(false);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleResourcePack(this);
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
