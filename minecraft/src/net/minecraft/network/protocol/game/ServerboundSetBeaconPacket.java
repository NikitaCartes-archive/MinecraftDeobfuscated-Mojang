package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
	private final MobEffect primary;
	private final MobEffect secondary;

	public ServerboundSetBeaconPacket(MobEffect mobEffect, MobEffect mobEffect2) {
		this.primary = mobEffect;
		this.secondary = mobEffect2;
	}

	public ServerboundSetBeaconPacket(FriendlyByteBuf friendlyByteBuf) {
		this.primary = friendlyByteBuf.readById(Registry.MOB_EFFECT);
		this.secondary = friendlyByteBuf.readById(Registry.MOB_EFFECT);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeId(Registry.MOB_EFFECT, this.primary);
		friendlyByteBuf.writeId(Registry.MOB_EFFECT, this.secondary);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetBeaconPacket(this);
	}

	public MobEffect getPrimary() {
		return this.primary;
	}

	public MobEffect getSecondary() {
		return this.secondary;
	}
}
