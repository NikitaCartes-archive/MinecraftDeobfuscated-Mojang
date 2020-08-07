package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DragonBreathParticle extends TextureSheetParticle {
	private boolean hasHitGround;
	private final SpriteSet sprites;

	private DragonBreathParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.rCol = Mth.nextFloat(this.random, 0.7176471F, 0.8745098F);
		this.gCol = Mth.nextFloat(this.random, 0.0F, 0.0F);
		this.bCol = Mth.nextFloat(this.random, 0.8235294F, 0.9764706F);
		this.quadSize *= 0.75F;
		this.lifetime = (int)(20.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
		this.hasHitGround = false;
		this.hasPhysics = false;
		this.sprites = spriteSet;
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
			if (this.onGround) {
				this.yd = 0.0;
				this.hasHitGround = true;
			}

			if (this.hasHitGround) {
				this.yd += 0.002;
			}

			this.move(this.xd, this.yd, this.zd);
			if (this.y == this.yo) {
				this.xd *= 1.1;
				this.zd *= 1.1;
			}

			this.xd *= 0.96F;
			this.zd *= 0.96F;
			if (this.hasHitGround) {
				this.yd *= 0.96F;
			}
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new DragonBreathParticle(clientLevel, d, e, f, g, h, i, this.sprites);
		}
	}
}
