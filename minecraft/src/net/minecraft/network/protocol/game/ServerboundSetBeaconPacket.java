package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
	private final Optional<MobEffect> primary;
	private final Optional<MobEffect> secondary;

	public ServerboundSetBeaconPacket(Optional<MobEffect> optional, Optional<MobEffect> optional2) {
		this.primary = optional;
		this.secondary = optional2;
	}

	public ServerboundSetBeaconPacket(FriendlyByteBuf friendlyByteBuf) {
		this.primary = friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readById(BuiltInRegistries.MOB_EFFECT));
		this.secondary = friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readById(BuiltInRegistries.MOB_EFFECT));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeOptional(this.primary, (friendlyByteBufx, mobEffect) -> friendlyByteBufx.writeId(BuiltInRegistries.MOB_EFFECT, mobEffect));
		friendlyByteBuf.writeOptional(this.secondary, (friendlyByteBufx, mobEffect) -> friendlyByteBufx.writeId(BuiltInRegistries.MOB_EFFECT, mobEffect));
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetBeaconPacket(this);
	}

	public Optional<MobEffect> getPrimary() {
		return this.primary;
	}

	public Optional<MobEffect> getSecondary() {
		return this.secondary;
	}
}
