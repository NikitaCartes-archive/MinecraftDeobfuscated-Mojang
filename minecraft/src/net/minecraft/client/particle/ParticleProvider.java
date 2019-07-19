package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public interface ParticleProvider<T extends ParticleOptions> {
	@Nullable
	Particle createParticle(T particleOptions, Level level, double d, double e, double f, double g, double h, double i);
}
