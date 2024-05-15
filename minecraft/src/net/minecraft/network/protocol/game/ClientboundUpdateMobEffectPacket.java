package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateMobEffectPacket> STREAM_CODEC = Packet.codec(
		ClientboundUpdateMobEffectPacket::write, ClientboundUpdateMobEffectPacket::new
	);
	private static final int FLAG_AMBIENT = 1;
	private static final int FLAG_VISIBLE = 2;
	private static final int FLAG_SHOW_ICON = 4;
	private static final int FLAG_BLEND = 8;
	private final int entityId;
	private final Holder<MobEffect> effect;
	private final int effectAmplifier;
	private final int effectDurationTicks;
	private final byte flags;

	public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance, boolean bl) {
		this.entityId = i;
		this.effect = mobEffectInstance.getEffect();
		this.effectAmplifier = mobEffectInstance.getAmplifier();
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

	private ClientboundUpdateMobEffectPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.entityId = registryFriendlyByteBuf.readVarInt();
		this.effect = MobEffect.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.effectAmplifier = registryFriendlyByteBuf.readVarInt();
		this.effectDurationTicks = registryFriendlyByteBuf.readVarInt();
		this.flags = registryFriendlyByteBuf.readByte();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeVarInt(this.entityId);
		MobEffect.STREAM_CODEC.encode(registryFriendlyByteBuf, this.effect);
		registryFriendlyByteBuf.writeVarInt(this.effectAmplifier);
		registryFriendlyByteBuf.writeVarInt(this.effectDurationTicks);
		registryFriendlyByteBuf.writeByte(this.flags);
	}

	@Override
	public PacketType<ClientboundUpdateMobEffectPacket> type() {
		return GamePacketTypes.CLIENTBOUND_UPDATE_MOB_EFFECT;
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

	public int getEffectAmplifier() {
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
