package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record ClientboundRemoveMobEffectPacket(int entityId, Holder<MobEffect> effect) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRemoveMobEffectPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		clientboundRemoveMobEffectPacket -> clientboundRemoveMobEffectPacket.entityId,
		ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT),
		ClientboundRemoveMobEffectPacket::effect,
		ClientboundRemoveMobEffectPacket::new
	);

	@Override
	public PacketType<ClientboundRemoveMobEffectPacket> type() {
		return GamePacketTypes.CLIENTBOUND_REMOVE_MOB_EFFECT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveMobEffect(this);
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}
}
