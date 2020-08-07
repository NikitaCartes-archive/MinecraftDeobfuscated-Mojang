package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class TrackingEmitter extends NoRenderParticle {
	private final Entity entity;
	private int life;
	private final int lifeTime;
	private final ParticleOptions particleType;

	public TrackingEmitter(ClientLevel clientLevel, Entity entity, ParticleOptions particleOptions) {
		this(clientLevel, entity, particleOptions, 3);
	}

	public TrackingEmitter(ClientLevel clientLevel, Entity entity, ParticleOptions particleOptions, int i) {
		this(clientLevel, entity, particleOptions, i, entity.getDeltaMovement());
	}

	private TrackingEmitter(ClientLevel clientLevel, Entity entity, ParticleOptions particleOptions, int i, Vec3 vec3) {
		super(clientLevel, entity.getX(), entity.getY(0.5), entity.getZ(), vec3.x, vec3.y, vec3.z);
		this.entity = entity;
		this.lifeTime = i;
		this.particleType = particleOptions;
		this.tick();
	}

	@Override
	public void tick() {
		for (int i = 0; i < 16; i++) {
			double d = (double)(this.random.nextFloat() * 2.0F - 1.0F);
			double e = (double)(this.random.nextFloat() * 2.0F - 1.0F);
			double f = (double)(this.random.nextFloat() * 2.0F - 1.0F);
			if (!(d * d + e * e + f * f > 1.0)) {
				double g = this.entity.getX(d / 4.0);
				double h = this.entity.getY(0.5 + e / 4.0);
				double j = this.entity.getZ(f / 4.0);
				this.level.addParticle(this.particleType, false, g, h, j, d, e + 0.2, f);
			}
		}

		this.life++;
		if (this.life >= this.lifeTime) {
			this.remove();
		}
	}
}
