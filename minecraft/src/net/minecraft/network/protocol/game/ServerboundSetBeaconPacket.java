package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public record ServerboundSetBeaconPacket(Optional<Holder<MobEffect>> primary, Optional<Holder<MobEffect>> secondary) implements Packet<ServerGamePacketListener> {
	public ServerboundSetBeaconPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readById(BuiltInRegistries.MOB_EFFECT.asHolderIdMap())),
			friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readById(BuiltInRegistries.MOB_EFFECT.asHolderIdMap()))
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeOptional(this.primary, (friendlyByteBufx, holder) -> friendlyByteBufx.writeId(BuiltInRegistries.MOB_EFFECT.asHolderIdMap(), holder));
		friendlyByteBuf.writeOptional(this.secondary, (friendlyByteBufx, holder) -> friendlyByteBufx.writeId(BuiltInRegistries.MOB_EFFECT.asHolderIdMap(), holder));
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetBeaconPacket(this);
	}
}
