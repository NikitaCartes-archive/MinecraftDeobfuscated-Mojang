package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
	private static final int FLAG_AMBIENT = 1;
	private static final int FLAG_VISIBLE = 2;
	private static final int FLAG_SHOW_ICON = 4;
	private final int entityId;
	private final MobEffect effect;
	private final byte effectAmplifier;
	private final int effectDurationTicks;
	private final byte flags;
	@Nullable
	private final MobEffectInstance.FactorData factorData;

	public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance) {
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

		this.flags = b;
		this.factorData = (MobEffectInstance.FactorData)mobEffectInstance.getFactorData().orElse(null);
	}

	public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.effect = friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT);
		this.effectAmplifier = friendlyByteBuf.readByte();
		this.effectDurationTicks = friendlyByteBuf.readVarInt();
		this.flags = friendlyByteBuf.readByte();
		this.factorData = friendlyByteBuf.readNullable(friendlyByteBufx -> friendlyByteBufx.readWithCodec(MobEffectInstance.FactorData.CODEC));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
		friendlyByteBuf.writeByte(this.effectAmplifier);
		friendlyByteBuf.writeVarInt(this.effectDurationTicks);
		friendlyByteBuf.writeByte(this.flags);
		friendlyByteBuf.writeNullable(
			this.factorData, (friendlyByteBufx, factorData) -> friendlyByteBufx.writeWithCodec(MobEffectInstance.FactorData.CODEC, factorData)
		);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateMobEffect(this);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public MobEffect getEffect() {
		return this.effect;
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

	@Nullable
	public MobEffectInstance.FactorData getFactorData() {
		return this.factorData;
	}
}
