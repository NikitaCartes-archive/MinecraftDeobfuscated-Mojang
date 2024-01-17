package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundExplodePacket> STREAM_CODEC = Packet.codec(
		ClientboundExplodePacket::write, ClientboundExplodePacket::new
	);
	private final double x;
	private final double y;
	private final double z;
	private final float power;
	private final List<BlockPos> toBlow;
	private final float knockbackX;
	private final float knockbackY;
	private final float knockbackZ;
	private final ParticleOptions smallExplosionParticles;
	private final ParticleOptions largeExplosionParticles;
	private final Explosion.BlockInteraction blockInteraction;
	private final Holder<SoundEvent> explosionSound;

	public ClientboundExplodePacket(
		double d,
		double e,
		double f,
		float g,
		List<BlockPos> list,
		@Nullable Vec3 vec3,
		Explosion.BlockInteraction blockInteraction,
		ParticleOptions particleOptions,
		ParticleOptions particleOptions2,
		Holder<SoundEvent> holder
	) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.power = g;
		this.toBlow = Lists.<BlockPos>newArrayList(list);
		this.explosionSound = holder;
		if (vec3 != null) {
			this.knockbackX = (float)vec3.x;
			this.knockbackY = (float)vec3.y;
			this.knockbackZ = (float)vec3.z;
		} else {
			this.knockbackX = 0.0F;
			this.knockbackY = 0.0F;
			this.knockbackZ = 0.0F;
		}

		this.blockInteraction = blockInteraction;
		this.smallExplosionParticles = particleOptions;
		this.largeExplosionParticles = particleOptions2;
	}

	private ClientboundExplodePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.x = registryFriendlyByteBuf.readDouble();
		this.y = registryFriendlyByteBuf.readDouble();
		this.z = registryFriendlyByteBuf.readDouble();
		this.power = registryFriendlyByteBuf.readFloat();
		int i = Mth.floor(this.x);
		int j = Mth.floor(this.y);
		int k = Mth.floor(this.z);
		this.toBlow = registryFriendlyByteBuf.readList(friendlyByteBuf -> {
			int l = friendlyByteBuf.readByte() + i;
			int m = friendlyByteBuf.readByte() + j;
			int n = friendlyByteBuf.readByte() + k;
			return new BlockPos(l, m, n);
		});
		this.knockbackX = registryFriendlyByteBuf.readFloat();
		this.knockbackY = registryFriendlyByteBuf.readFloat();
		this.knockbackZ = registryFriendlyByteBuf.readFloat();
		this.blockInteraction = registryFriendlyByteBuf.readEnum(Explosion.BlockInteraction.class);
		this.smallExplosionParticles = ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.largeExplosionParticles = ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.explosionSound = SoundEvent.STREAM_CODEC.decode(registryFriendlyByteBuf);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeDouble(this.x);
		registryFriendlyByteBuf.writeDouble(this.y);
		registryFriendlyByteBuf.writeDouble(this.z);
		registryFriendlyByteBuf.writeFloat(this.power);
		int i = Mth.floor(this.x);
		int j = Mth.floor(this.y);
		int k = Mth.floor(this.z);
		registryFriendlyByteBuf.writeCollection(this.toBlow, (friendlyByteBuf, blockPos) -> {
			int l = blockPos.getX() - i;
			int m = blockPos.getY() - j;
			int n = blockPos.getZ() - k;
			friendlyByteBuf.writeByte(l);
			friendlyByteBuf.writeByte(m);
			friendlyByteBuf.writeByte(n);
		});
		registryFriendlyByteBuf.writeFloat(this.knockbackX);
		registryFriendlyByteBuf.writeFloat(this.knockbackY);
		registryFriendlyByteBuf.writeFloat(this.knockbackZ);
		registryFriendlyByteBuf.writeEnum(this.blockInteraction);
		ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.smallExplosionParticles);
		ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.largeExplosionParticles);
		SoundEvent.STREAM_CODEC.encode(registryFriendlyByteBuf, this.explosionSound);
	}

	@Override
	public PacketType<ClientboundExplodePacket> type() {
		return GamePacketTypes.CLIENTBOUND_EXPLODE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleExplosion(this);
	}

	public float getKnockbackX() {
		return this.knockbackX;
	}

	public float getKnockbackY() {
		return this.knockbackY;
	}

	public float getKnockbackZ() {
		return this.knockbackZ;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public float getPower() {
		return this.power;
	}

	public List<BlockPos> getToBlow() {
		return this.toBlow;
	}

	public Explosion.BlockInteraction getBlockInteraction() {
		return this.blockInteraction;
	}

	public ParticleOptions getSmallExplosionParticles() {
		return this.smallExplosionParticles;
	}

	public ParticleOptions getLargeExplosionParticles() {
		return this.largeExplosionParticles;
	}

	public Holder<SoundEvent> getExplosionSound() {
		return this.explosionSound;
	}
}
