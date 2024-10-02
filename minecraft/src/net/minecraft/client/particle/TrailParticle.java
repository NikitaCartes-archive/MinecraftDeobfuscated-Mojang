package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.TargetColorParticleOption;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class TrailParticle extends TextureSheetParticle {
	private final Vec3 target;

	TrailParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, Vec3 vec3, int j) {
		super(clientLevel, d, e, f, g, h, i);
		j = ARGB.scaleRGB(j, 0.875F + this.random.nextFloat() * 0.25F, 0.875F + this.random.nextFloat() * 0.25F, 0.875F + this.random.nextFloat() * 0.25F);
		this.rCol = (float)ARGB.red(j) / 255.0F;
		this.gCol = (float)ARGB.green(j) / 255.0F;
		this.bCol = (float)ARGB.blue(j) / 255.0F;
		this.quadSize = 0.26F;
		this.target = vec3;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			int i = this.lifetime - this.age;
			double d = 1.0 / (double)i;
			this.x = Mth.lerp(d, this.x, this.target.x());
			this.y = Mth.lerp(d, this.y, this.target.y());
			this.z = Mth.lerp(d, this.z, this.target.z());
		}
	}

	@Override
	public int getLightColor(float f) {
		return 15728880;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<TargetColorParticleOption> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(
			TargetColorParticleOption targetColorParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i
		) {
			TrailParticle trailParticle = new TrailParticle(clientLevel, d, e, f, g, h, i, targetColorParticleOption.target(), targetColorParticleOption.color());
			trailParticle.pickSprite(this.sprite);
			trailParticle.setLifetime(clientLevel.random.nextInt(40) + 10);
			return trailParticle;
		}
	}
}
