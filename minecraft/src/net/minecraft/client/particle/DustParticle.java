package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class DustParticle extends DustParticleBase<DustParticleOptions> {
	protected DustParticle(
		ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, DustParticleOptions dustParticleOptions, SpriteSet spriteSet
	) {
		super(clientLevel, d, e, f, g, h, i, dustParticleOptions, spriteSet);
		float j = this.random.nextFloat() * 0.4F + 0.6F;
		Vector3f vector3f = dustParticleOptions.getColor();
		this.rCol = this.randomizeColor(vector3f.x(), j);
		this.gCol = this.randomizeColor(vector3f.y(), j);
		this.bCol = this.randomizeColor(vector3f.z(), j);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<DustParticleOptions> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(DustParticleOptions dustParticleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new DustParticle(clientLevel, d, e, f, g, h, i, dustParticleOptions, this.sprites);
		}
	}
}
