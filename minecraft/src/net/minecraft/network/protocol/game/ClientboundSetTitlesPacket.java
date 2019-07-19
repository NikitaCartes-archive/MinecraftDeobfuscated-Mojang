package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTitlesPacket implements Packet<ClientGamePacketListener> {
	private ClientboundSetTitlesPacket.Type type;
	private Component text;
	private int fadeInTime;
	private int stayTime;
	private int fadeOutTime;

	public ClientboundSetTitlesPacket() {
	}

	public ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type type, Component component) {
		this(type, component, -1, -1, -1);
	}

	public ClientboundSetTitlesPacket(int i, int j, int k) {
		this(ClientboundSetTitlesPacket.Type.TIMES, null, i, j, k);
	}

	public ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type type, @Nullable Component component, int i, int j, int k) {
		this.type = type;
		this.text = component;
		this.fadeInTime = i;
		this.stayTime = j;
		this.fadeOutTime = k;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.type = friendlyByteBuf.readEnum(ClientboundSetTitlesPacket.Type.class);
		if (this.type == ClientboundSetTitlesPacket.Type.TITLE
			|| this.type == ClientboundSetTitlesPacket.Type.SUBTITLE
			|| this.type == ClientboundSetTitlesPacket.Type.ACTIONBAR) {
			this.text = friendlyByteBuf.readComponent();
		}

		if (this.type == ClientboundSetTitlesPacket.Type.TIMES) {
			this.fadeInTime = friendlyByteBuf.readInt();
			this.stayTime = friendlyByteBuf.readInt();
			this.fadeOutTime = friendlyByteBuf.readInt();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.type);
		if (this.type == ClientboundSetTitlesPacket.Type.TITLE
			|| this.type == ClientboundSetTitlesPacket.Type.SUBTITLE
			|| this.type == ClientboundSetTitlesPacket.Type.ACTIONBAR) {
			friendlyByteBuf.writeComponent(this.text);
		}

		if (this.type == ClientboundSetTitlesPacket.Type.TIMES) {
			friendlyByteBuf.writeInt(this.fadeInTime);
			friendlyByteBuf.writeInt(this.stayTime);
			friendlyByteBuf.writeInt(this.fadeOutTime);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetTitles(this);
	}

	@Environment(EnvType.CLIENT)
	public ClientboundSetTitlesPacket.Type getType() {
		return this.type;
	}

	@Environment(EnvType.CLIENT)
	public Component getText() {
		return this.text;
	}

	@Environment(EnvType.CLIENT)
	public int getFadeInTime() {
		return this.fadeInTime;
	}

	@Environment(EnvType.CLIENT)
	public int getStayTime() {
		return this.stayTime;
	}

	@Environment(EnvType.CLIENT)
	public int getFadeOutTime() {
		return this.fadeOutTime;
	}

	public static enum Type {
		TITLE,
		SUBTITLE,
		ACTIONBAR,
		TIMES,
		CLEAR,
		RESET;
	}
}
