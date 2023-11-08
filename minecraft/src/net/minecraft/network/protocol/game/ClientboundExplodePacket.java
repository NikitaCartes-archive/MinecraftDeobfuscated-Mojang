package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
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
	private final SoundEvent explosionSound;

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
		SoundEvent soundEvent
	) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.power = g;
		this.toBlow = Lists.<BlockPos>newArrayList(list);
		this.explosionSound = soundEvent;
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

	public ClientboundExplodePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.power = friendlyByteBuf.readFloat();
		int i = Mth.floor(this.x);
		int j = Mth.floor(this.y);
		int k = Mth.floor(this.z);
		this.toBlow = friendlyByteBuf.readList(friendlyByteBufx -> {
			int l = friendlyByteBufx.readByte() + i;
			int m = friendlyByteBufx.readByte() + j;
			int n = friendlyByteBufx.readByte() + k;
			return new BlockPos(l, m, n);
		});
		this.knockbackX = friendlyByteBuf.readFloat();
		this.knockbackY = friendlyByteBuf.readFloat();
		this.knockbackZ = friendlyByteBuf.readFloat();
		this.blockInteraction = friendlyByteBuf.readEnum(Explosion.BlockInteraction.class);
		this.smallExplosionParticles = readParticle(friendlyByteBuf, friendlyByteBuf.readById(BuiltInRegistries.PARTICLE_TYPE));
		this.largeExplosionParticles = readParticle(friendlyByteBuf, friendlyByteBuf.readById(BuiltInRegistries.PARTICLE_TYPE));
		this.explosionSound = SoundEvent.readFromNetwork(friendlyByteBuf);
	}

	private static <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
		return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.power);
		int i = Mth.floor(this.x);
		int j = Mth.floor(this.y);
		int k = Mth.floor(this.z);
		friendlyByteBuf.writeCollection(this.toBlow, (friendlyByteBufx, blockPos) -> {
			int l = blockPos.getX() - i;
			int m = blockPos.getY() - j;
			int n = blockPos.getZ() - k;
			friendlyByteBufx.writeByte(l);
			friendlyByteBufx.writeByte(m);
			friendlyByteBufx.writeByte(n);
		});
		friendlyByteBuf.writeFloat(this.knockbackX);
		friendlyByteBuf.writeFloat(this.knockbackY);
		friendlyByteBuf.writeFloat(this.knockbackZ);
		friendlyByteBuf.writeEnum(this.blockInteraction);
		friendlyByteBuf.writeId(BuiltInRegistries.PARTICLE_TYPE, this.smallExplosionParticles.getType());
		friendlyByteBuf.writeId(BuiltInRegistries.PARTICLE_TYPE, this.largeExplosionParticles.getType());
		this.explosionSound.writeToNetwork(friendlyByteBuf);
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

	public SoundEvent getExplosionSound() {
		return this.explosionSound;
	}
}
