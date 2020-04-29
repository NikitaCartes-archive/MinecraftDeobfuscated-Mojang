package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

@Environment(EnvType.CLIENT)
public interface ParticleProvider<T extends ParticleOptions> {
	@Nullable
	Particle createParticle(T particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i);
}
