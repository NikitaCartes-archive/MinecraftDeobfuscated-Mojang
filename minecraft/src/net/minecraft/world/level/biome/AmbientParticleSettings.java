package net.minecraft.world.level.biome;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;

public class AmbientParticleSettings {
	private final ParticleOptions particleType;
	private final float probability;
	private final double xVelocity;
	private final double yVelocity;
	private final double zVelocity;

	public AmbientParticleSettings(ParticleOptions particleOptions, float f, double d, double e, double g) {
		this.particleType = particleOptions;
		this.probability = f;
		this.xVelocity = d;
		this.yVelocity = e;
		this.zVelocity = g;
	}

	@Environment(EnvType.CLIENT)
	public ParticleOptions getParticleType() {
		return this.particleType;
	}

	@Environment(EnvType.CLIENT)
	public boolean canSpawn(Random random) {
		return random.nextFloat() <= this.probability;
	}

	@Environment(EnvType.CLIENT)
	public double getXVelocity() {
		return this.xVelocity;
	}

	@Environment(EnvType.CLIENT)
	public double getYVelocity() {
		return this.yVelocity;
	}

	@Environment(EnvType.CLIENT)
	public double getZVelocity() {
		return this.zVelocity;
	}

	public static AmbientParticleSettings random(Random random) {
		return new AmbientParticleSettings(
			Registry.PARTICLE_TYPE.getRandom(random).getRandom(random), random.nextFloat() * 0.2F, random.nextDouble(), random.nextDouble(), random.nextDouble()
		);
	}
}
