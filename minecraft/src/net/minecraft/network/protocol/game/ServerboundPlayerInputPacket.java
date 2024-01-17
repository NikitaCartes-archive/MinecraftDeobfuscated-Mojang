package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerInputPacket> STREAM_CODEC = Packet.codec(
		ServerboundPlayerInputPacket::write, ServerboundPlayerInputPacket::new
	);
	private static final int FLAG_JUMPING = 1;
	private static final int FLAG_SHIFT_KEY_DOWN = 2;
	private final float xxa;
	private final float zza;
	private final boolean isJumping;
	private final boolean isShiftKeyDown;

	public ServerboundPlayerInputPacket(float f, float g, boolean bl, boolean bl2) {
		this.xxa = f;
		this.zza = g;
		this.isJumping = bl;
		this.isShiftKeyDown = bl2;
	}

	private ServerboundPlayerInputPacket(FriendlyByteBuf friendlyByteBuf) {
		this.xxa = friendlyByteBuf.readFloat();
		this.zza = friendlyByteBuf.readFloat();
		byte b = friendlyByteBuf.readByte();
		this.isJumping = (b & 1) > 0;
		this.isShiftKeyDown = (b & 2) > 0;
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.xxa);
		friendlyByteBuf.writeFloat(this.zza);
		byte b = 0;
		if (this.isJumping) {
			b = (byte)(b | 1);
		}

		if (this.isShiftKeyDown) {
			b = (byte)(b | 2);
		}

		friendlyByteBuf.writeByte(b);
	}

	@Override
	public PacketType<ServerboundPlayerInputPacket> type() {
		return GamePacketTypes.SERVERBOUND_PLAYER_INPUT;
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

	public boolean isShiftKeyDown() {
		return this.isShiftKeyDown;
	}
}
