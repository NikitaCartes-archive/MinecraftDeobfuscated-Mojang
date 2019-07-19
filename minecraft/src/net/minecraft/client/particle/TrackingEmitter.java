package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class TrackingEmitter extends NoRenderParticle {
	private final Entity entity;
	private int life;
	private final int lifeTime;
	private final ParticleOptions particleType;

	public TrackingEmitter(Level level, Entity entity, ParticleOptions particleOptions) {
		this(level, entity, particleOptions, 3);
	}

	public TrackingEmitter(Level level, Entity entity, ParticleOptions particleOptions, int i) {
		this(level, entity, particleOptions, i, entity.getDeltaMovement());
	}

	private TrackingEmitter(Level level, Entity entity, ParticleOptions particleOptions, int i, Vec3 vec3) {
		super(level, entity.x, entity.getBoundingBox().minY + (double)(entity.getBbHeight() / 2.0F), entity.z, vec3.x, vec3.y, vec3.z);
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
				double g = this.entity.x + d * (double)this.entity.getBbWidth() / 4.0;
				double h = this.entity.getBoundingBox().minY + (double)(this.entity.getBbHeight() / 2.0F) + e * (double)this.entity.getBbHeight() / 4.0;
				double j = this.entity.z + f * (double)this.entity.getBbWidth() / 4.0;
				this.level.addParticle(this.particleType, false, g, h, j, d, e + 0.2, f);
			}
		}

		this.life++;
		if (this.life >= this.lifeTime) {
			this.remove();
		}
	}
}
