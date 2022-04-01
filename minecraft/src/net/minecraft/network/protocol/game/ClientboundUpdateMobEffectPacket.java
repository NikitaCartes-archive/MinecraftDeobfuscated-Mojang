package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
	private static final int FLAG_AMBIENT = 1;
	private static final int FLAG_VISIBLE = 2;
	private static final int FLAG_SHOW_ICON = 4;
	private final int entityId;
	private final int effectId;
	private final byte effectAmplifier;
	private final int effectDurationTicks;
	private final byte flags;

	public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance) {
		this.entityId = i;
		this.effectId = MobEffect.getId(mobEffectInstance.getEffect());
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
		this.effectId = friendlyByteBuf.readVarInt();
		this.effectAmplifier = friendlyByteBuf.readByte();
		this.effectDurationTicks = friendlyByteBuf.readVarInt();
		this.flags = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeVarInt(this.effectId);
		friendlyByteBuf.writeByte(this.effectAmplifier);
		friendlyByteBuf.writeVarInt(this.effectDurationTicks);
		friendlyByteBuf.writeByte(this.flags);
	}

	public boolean isSuperLongDuration() {
		return this.effectDurationTicks == 32767;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateMobEffect(this);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public int getEffectId() {
		return this.effectId;
	}

	public byte getEffectAmplifier() {
		return this.effectAmplifier;
	}

	public int getEffectDurationTicks() {
		return this.effectDurationTicks;
	}

	public boolean isEffectVisible() {
		return (this.flags & 2) == 2;
	}

	public boolean isEffectAmbient() {
		return (this.flags & 1) == 1;
	}

	public boolean effectShowsIcon() {
		return (this.flags & 4) == 4;
	}
}
