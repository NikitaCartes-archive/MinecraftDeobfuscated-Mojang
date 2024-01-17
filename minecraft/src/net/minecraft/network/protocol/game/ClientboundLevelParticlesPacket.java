package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelParticlesPacket> STREAM_CODEC = Packet.codec(
		ClientboundLevelParticlesPacket::write, ClientboundLevelParticlesPacket::new
	);
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

	private ClientboundLevelParticlesPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.overrideLimiter = registryFriendlyByteBuf.readBoolean();
		this.x = registryFriendlyByteBuf.readDouble();
		this.y = registryFriendlyByteBuf.readDouble();
		this.z = registryFriendlyByteBuf.readDouble();
		this.xDist = registryFriendlyByteBuf.readFloat();
		this.yDist = registryFriendlyByteBuf.readFloat();
		this.zDist = registryFriendlyByteBuf.readFloat();
		this.maxSpeed = registryFriendlyByteBuf.readFloat();
		this.count = registryFriendlyByteBuf.readInt();
		this.particle = ParticleTypes.STREAM_CODEC.decode(registryFriendlyByteBuf);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeBoolean(this.overrideLimiter);
		registryFriendlyByteBuf.writeDouble(this.x);
		registryFriendlyByteBuf.writeDouble(this.y);
		registryFriendlyByteBuf.writeDouble(this.z);
		registryFriendlyByteBuf.writeFloat(this.xDist);
		registryFriendlyByteBuf.writeFloat(this.yDist);
		registryFriendlyByteBuf.writeFloat(this.zDist);
		registryFriendlyByteBuf.writeFloat(this.maxSpeed);
		registryFriendlyByteBuf.writeInt(this.count);
		ParticleTypes.STREAM_CODEC.encode(registryFriendlyByteBuf, this.particle);
	}

	@Override
	public PacketType<ClientboundLevelParticlesPacket> type() {
		return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleParticleEvent(this);
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
}
