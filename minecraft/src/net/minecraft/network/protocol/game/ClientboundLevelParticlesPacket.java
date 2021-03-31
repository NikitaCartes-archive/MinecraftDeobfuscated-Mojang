package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
	private final double x;
	private final double y;
	private final double z;
	private final float xDist;
	private final float yDist;
	private final float zDist;
	private final float maxSpeed;
	private final int count;
	private final boolean overrideLimiter;
	private final ParticleOptions particle;

	public <T extends ParticleOptions> ClientboundLevelParticlesPacket(
		T particleOptions, boolean bl, double d, double e, double f, float g, float h, float i, float j, int k
	) {
		this.particle = particleOptions;
		this.overrideLimiter = bl;
		this.x = d;
		this.y = e;
		this.z = f;
		this.xDist = g;
		this.yDist = h;
		this.zDist = i;
		this.maxSpeed = j;
		this.count = k;
	}

	public ClientboundLevelParticlesPacket(FriendlyByteBuf friendlyByteBuf) {
		ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(friendlyByteBuf.readInt());
		if (particleType == null) {
			particleType = ParticleTypes.BARRIER;
		}

		this.overrideLimiter = friendlyByteBuf.readBoolean();
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
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
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(Registry.PARTICLE_TYPE.getId(this.particle.getType()));
		friendlyByteBuf.writeBoolean(this.overrideLimiter);
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.xDist);
		friendlyByteBuf.writeFloat(this.yDist);
		friendlyByteBuf.writeFloat(this.zDist);
		friendlyByteBuf.writeFloat(this.maxSpeed);
		friendlyByteBuf.writeInt(this.count);
		this.particle.writeToNetwork(friendlyByteBuf);
	}

	public boolean isOverrideLimiter() {
		return this.overrideLimiter;
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

	public float getXDist() {
		return this.xDist;
	}

	public float getYDist() {
		return this.yDist;
	}

	public float getZDist() {
		return this.zDist;
	}

	public float getMaxSpeed() {
		return this.maxSpeed;
	}

	public int getCount() {
		return this.count;
	}

	public ParticleOptions getParticle() {
		return this.particle;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleParticleEvent(this);
	}
}
