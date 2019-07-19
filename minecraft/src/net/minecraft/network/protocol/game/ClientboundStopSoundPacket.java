package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ClientboundStopSoundPacket implements Packet<ClientGamePacketListener> {
	private ResourceLocation name;
	private SoundSource source;

	public ClientboundStopSoundPacket() {
	}

	public ClientboundStopSoundPacket(@Nullable ResourceLocation resourceLocation, @Nullable SoundSource soundSource) {
		this.name = resourceLocation;
		this.source = soundSource;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		int i = friendlyByteBuf.readByte();
		if ((i & 1) > 0) {
			this.source = friendlyByteBuf.readEnum(SoundSource.class);
		}

		if ((i & 2) > 0) {
			this.name = friendlyByteBuf.readResourceLocation();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
	@Environment(EnvType.CLIENT)
	public ResourceLocation getName() {
		return this.name;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public SoundSource getSource() {
		return this.source;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleStopSoundEvent(this);
	}
}
