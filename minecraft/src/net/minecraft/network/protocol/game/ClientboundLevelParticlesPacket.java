package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
	private float x;
	private float y;
	private float z;
	private float xDist;
	private float yDist;
	private float zDist;
	private float maxSpeed;
	private int count;
	private boolean overrideLimiter;
	private ParticleOptions particle;

	public ClientboundLevelParticlesPacket() {
	}

	public <T extends ParticleOptions> ClientboundLevelParticlesPacket(
		T particleOptions, boolean bl, float f, float g, float h, float i, float j, float k, float l, int m
	) {
		this.particle = particleOptions;
		this.overrideLimiter = bl;
		this.x = f;
		this.y = g;
		this.z = h;
		this.xDist = i;
		this.yDist = j;
		this.zDist = k;
		this.maxSpeed = l;
		this.count = m;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(friendlyByteBuf.readInt());
		if (particleType == null) {
			particleType = ParticleTypes.BARRIER;
		}

		this.overrideLimiter = friendlyByteBuf.readBoolean();
		this.x = friendlyByteBuf.readFloat();
		this.y = friendlyByteBuf.readFloat();
		this.z = friendlyByteBuf.readFloat();
		this.xDist = friendlyByteBuf.readFloat();
		this.yDist = friendlyByteBuf.readFloat();
		this.zDist = friendlyByteBuf.readFloat();
		this.maxSpeed = friendlyByteBuf.readFloat();
		this.count = friendlyByteBuf.readInt();
		this.particle = this.readParticle(friendlyByteBuf, (ParticleType<ParticleOptions>)particleType);
	}

	private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlyByteBuf, ParticleType<T> particleType) {
		return particleType.getDeserializer().fromNetwork(particleType, friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(Registry.PARTICLE_TYPE.getId((ParticleType<? extends ParticleOptions>)this.particle.getType()));
		friendlyByteBuf.writeBoolean(this.overrideLimiter);
		friendlyByteBuf.writeFloat(this.x);
		friendlyByteBuf.writeFloat(this.y);
		friendlyByteBuf.writeFloat(this.z);
		friendlyByteBuf.writeFloat(this.xDist);
		friendlyByteBuf.writeFloat(this.yDist);
		friendlyByteBuf.writeFloat(this.zDist);
		friendlyByteBuf.writeFloat(this.maxSpeed);
		friendlyByteBuf.writeInt(this.count);
		this.particle.writeToNetwork(friendlyByteBuf);
	}

	@Environment(EnvType.CLIENT)
	public boolean isOverrideLimiter() {
		return this.overrideLimiter;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return (double)this.x;
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return (double)this.y;
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return (double)this.z;
	}

	@Environment(EnvType.CLIENT)
	public float getXDist() {
		return this.xDist;
	}

	@Environment(EnvType.CLIENT)
	public float getYDist() {
		return this.yDist;
	}

	@Environment(EnvType.CLIENT)
	public float getZDist() {
		return this.zDist;
	}

	@Environment(EnvType.CLIENT)
	public float getMaxSpeed() {
		return this.maxSpeed;
	}

	@Environment(EnvType.CLIENT)
	public int getCount() {
		return this.count;
	}

	@Environment(EnvType.CLIENT)
	public ParticleOptions getParticle() {
		return this.particle;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleParticleEvent(this);
	}
}
