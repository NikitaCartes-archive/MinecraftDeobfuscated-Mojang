package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ServerboundPlayerAbilitiesPacket implements Packet<ServerGamePacketListener> {
	private final boolean isFlying;

	public ServerboundPlayerAbilitiesPacket(Abilities abilities) {
		this.isFlying = abilities.flying;
	}

	public ServerboundPlayerAbilitiesPacket(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		this.isFlying = (b & 2) != 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		byte b = 0;
		if (this.isFlying) {
			b = (byte)(b | 2);
		}

		friendlyByteBuf.writeByte(b);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePlayerAbilities(this);
	}

	public boolean isFlying() {
		return this.isFlying;
	}
}
