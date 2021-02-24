package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;
	private final byte effectId;
	private final byte effectAmplifier;
	private final int effectDurationTicks;
	private final byte flags;

	public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance) {
		this.entityId = i;
		this.effectId = (byte)(MobEffect.getId(mobEffectInstance.getEffect()) & 0xFF);
		this.effectAmplifier = (byte)(mobEffectInstance.getAmplifier() & 0xFF);
		if (mobEffectInstance.getDuration() > 32767) {
			this.effectDurationTicks = 32767;
		} else {
			this.effectDurationTicks = mobEffectInstance.getDuration();
		}

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

		this.flags = b;
	}

	public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.effectId = friendlyByteBuf.readByte();
		this.effectAmplifier = friendlyByteBuf.readByte();
		this.effectDurationTicks = friendlyByteBuf.readVarInt();
		this.flags = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeByte(this.effectId);
		friendlyByteBuf.writeByte(this.effectAmplifier);
		friendlyByteBuf.writeVarInt(this.effectDurationTicks);
		friendlyByteBuf.writeByte(this.flags);
	}

	@Environment(EnvType.CLIENT)
	public boolean isSuperLongDuration() {
		return this.effectDurationTicks == 32767;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateMobEffect(this);
	}

	@Environment(EnvType.CLIENT)
	public int getEntityId() {
		return this.entityId;
	}

	@Environment(EnvType.CLIENT)
	public byte getEffectId() {
		return this.effectId;
	}

	@Environment(EnvType.CLIENT)
	public byte getEffectAmplifier() {
		return this.effectAmplifier;
	}

	@Environment(EnvType.CLIENT)
	public int getEffectDurationTicks() {
		return this.effectDurationTicks;
	}

	@Environment(EnvType.CLIENT)
	public boolean isEffectVisible() {
		return (this.flags & 2) == 2;
	}

	@Environment(EnvType.CLIENT)
	public boolean isEffectAmbient() {
		return (this.flags & 1) == 1;
	}

	@Environment(EnvType.CLIENT)
	public boolean effectShowsIcon() {
		return (this.flags & 4) == 4;
	}
}
