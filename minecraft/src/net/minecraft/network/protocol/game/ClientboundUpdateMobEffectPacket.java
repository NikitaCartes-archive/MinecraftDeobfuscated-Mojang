package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
	private static final int FLAG_AMBIENT = 1;
	private static final int FLAG_VISIBLE = 2;
	private static final int FLAG_SHOW_ICON = 4;
	private static final int FLAG_BLEND = 8;
	private final int entityId;
	private final Holder<MobEffect> effect;
	private final byte effectAmplifier;
	private final int effectDurationTicks;
	private final byte flags;

	public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance, boolean bl) {
		this.entityId = i;
		this.effect = mobEffectInstance.getEffect();
		this.effectAmplifier = (byte)(mobEffectInstance.getAmplifier() & 0xFF);
		this.effectDurationTicks = mobEffectInstance.getDuration();
		byte b = 0;
		if (mobEffectInstance.isAmbient()) {
			b = (byte)(b | 1);
		}

		if (mobEffectInstance.isVisible()) {
			b = (byte)(b | 2);
		}

		if (mobEffectInstance.showIcon()) {
			b = (byte)(b | 4);
		}

		if (bl) {
			b = (byte)(b | 8);
		}

		this.flags = b;
	}

	public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.effect = friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT.asHolderIdMap());
		this.effectAmplifier = friendlyByteBuf.readByte();
		this.effectDurationTicks = friendlyByteBuf.readVarInt();
		this.flags = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT.asHolderIdMap(), this.effect);
		friendlyByteBuf.writeByte(this.effectAmplifier);
		friendlyByteBuf.writeVarInt(this.effectDurationTicks);
		friendlyByteBuf.writeByte(this.flags);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateMobEffect(this);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public Holder<MobEffect> getEffect() {
		return this.effect;
	}

	public byte getEffectAmplifier() {
		return this.effectAmplifier;
	}

	public int getEffectDurationTicks() {
		return this.effectDurationTicks;
	}

	public boolean isEffectVisible() {
		return (this.flags & 2) != 0;
	}

	public boolean isEffectAmbient() {
		return (this.flags & 1) != 0;
	}

	public boolean effectShowsIcon() {
		return (this.flags & 4) != 0;
	}

	public boolean shouldBlend() {
		return (this.flags & 8) != 0;
	}
}
