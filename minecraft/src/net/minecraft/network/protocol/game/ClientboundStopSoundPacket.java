package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ClientboundStopSoundPacket implements Packet<ClientGamePacketListener> {
	private static final int HAS_SOURCE = 1;
	private static final int HAS_SOUND = 2;
	@Nullable
	private final ResourceLocation name;
	@Nullable
	private final SoundSource source;

	public ClientboundStopSoundPacket(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
		this.name = resourceLocation;
		this.source = soundSource;
	}

	public ClientboundStopSoundPacket(FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readByte();
		if ((i & 1) > 0) {
			this.source = friendlyByteBuf.readEnum(SoundSource.class);
		} else {
			this.source = null;
		}

		if ((i & 2) > 0) {
			this.name = friendlyByteBuf.readResourceLocation();
		} else {
			this.name = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		if (this.source != null) {
			if (this.name != null) {
				friendlyByteBuf.writeByte(3);
				friendlyByteBuf.writeEnum(this.source);
				friendlyByteBuf.writeResourceLocation(this.name);
			} else {
				friendlyByteBuf.writeByte(1);
				friendlyByteBuf.writeEnum(this.source);
			}
		} else if (this.name != null) {
			friendlyByteBuf.writeByte(2);
			friendlyByteBuf.writeResourceLocation(this.name);
		} else {
			friendlyByteBuf.writeByte(0);
		}
	}

	@Nullable
	public ResourceLocation getName() {
		return this.name;
	}

	@Nullable
	public SoundSource getSource() {
		return this.source;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleStopSoundEvent(this);
	}
}
