package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

@Environment(EnvType.CLIENT)
public class SquidInkParticle extends SimpleAnimatedParticle {
	private SquidInkParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, int j, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, spriteSet, 0.0F);
		this.quadSize = 0.5F;
		this.setAlpha(1.0F);
		this.setColor((float)FastColor.ARGB32.red(j), (float)FastColor.ARGB32.green(j), (float)FastColor.ARGB32.blue(j));
		this.lifetime = (int)((double)(this.quadSize * 12.0F) / (Math.random() * 0.8F + 0.2F));
		this.setSpriteFromAge(spriteSet);
		this.hasPhysics = false;
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.setBaseAirFriction(0.0F);
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
			if (this.age > this.lifetime / 2) {
				this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
			}

			this.move(this.xd, this.yd, this.zd);
			if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
				this.yd -= 0.008F;
			}

			this.xd *= 0.92F;
			this.yd *= 0.92F;
			this.zd *= 0.92F;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class GlowInkProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public GlowInkProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SquidInkParticle(clientLevel, d, e, f, g, h, i, FastColor.ARGB32.color(255, 204, 31, 102), this.sprites);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new SquidInkParticle(clientLevel, d, e, f, g, h, i, FastColor.ARGB32.color(255, 255, 255, 255), this.sprites);
		}
	}
}
