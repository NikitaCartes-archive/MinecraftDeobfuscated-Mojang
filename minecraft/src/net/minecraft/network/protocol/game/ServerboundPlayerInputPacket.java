package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
	private float xxa;
	private float zza;
	private boolean isJumping;
	private boolean isSneaking;

	public ServerboundPlayerInputPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundPlayerInputPacket(float f, float g, boolean bl, boolean bl2) {
		this.xxa = f;
		this.zza = g;
		this.isJumping = bl;
		this.isSneaking = bl2;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.xxa = friendlyByteBuf.readFloat();
		this.zza = friendlyByteBuf.readFloat();
		byte b = friendlyByteBuf.readByte();
		this.isJumping = (b & 1) > 0;
		this.isSneaking = (b & 2) > 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeFloat(this.xxa);
		friendlyByteBuf.writeFloat(this.zza);
		byte b = 0;
		if (this.isJumping) {
			b = (byte)(b | 1);
		}

		if (this.isSneaking) {
			b = (byte)(b | 2);
		}

		friendlyByteBuf.writeByte(b);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePlayerInput(this);
	}

	public float getXxa() {
		return this.xxa;
	}

	public float getZza() {
		return this.zza;
	}

	public boolean isJumping() {
		return this.isJumping;
	}

	public boolean isSneaking() {
		return this.isSneaking;
	}
}
