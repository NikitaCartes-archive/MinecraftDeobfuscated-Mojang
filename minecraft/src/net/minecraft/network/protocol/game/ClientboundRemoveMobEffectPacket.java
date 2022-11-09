package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;
	private final MobEffect effect;

	public ClientboundRemoveMobEffectPacket(int i, MobEffect mobEffect) {
		this.entityId = i;
		this.effect = mobEffect;
	}

	public ClientboundRemoveMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.effect = friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveMobEffect(this);
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	@Nullable
	public MobEffect getEffect() {
		return this.effect;
	}
}
