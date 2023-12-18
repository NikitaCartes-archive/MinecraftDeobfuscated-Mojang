package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record ClientboundRemoveMobEffectPacket(int entityId, Holder<MobEffect> effect) implements Packet<ClientGamePacketListener> {
	public ClientboundRemoveMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT.asHolderIdMap()));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT.asHolderIdMap(), this.effect);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveMobEffect(this);
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}
}
