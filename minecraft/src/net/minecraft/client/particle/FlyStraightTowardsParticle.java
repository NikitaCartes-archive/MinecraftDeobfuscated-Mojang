package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FlyStraightTowardsParticle extends TextureSheetParticle {
	private final double xStart;
	private final double yStart;
	private final double zStart;
	private final int startColor;
	private final int endColor;

	FlyStraightTowardsParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, int j, int k) {
		super(clientLevel, d, e, f);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.xStart = d;
		this.yStart = e;
		this.zStart = f;
		this.xo = d + g;
		this.yo = e + h;
		this.zo = f + i;
		this.x = this.xo;
		this.y = this.yo;
		this.z = this.zo;
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
		this.hasPhysics = false;
		this.lifetime = (int)(Math.random() * 5.0) + 25;
		this.startColor = j;
		this.endColor = k;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double d, double e, double f) {
	}

	@Override
	public int getLightColor(float f) {
		return 240;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			float f = (float)this.age / (float)this.lifetime;
			float g = 1.0F - f;
			this.x = this.xStart + this.xd * (double)g;
			this.y = this.yStart + this.yd * (double)g;
			this.z = this.zStart + this.zd * (double)g;
			int i = ARGB.lerp(f, this.startColor, this.endColor);
			this.setColor((float)ARGB.red(i) / 255.0F, (float)ARGB.green(i) / 255.0F, (float)ARGB.blue(i) / 255.0F);
			this.setAlpha((float)ARGB.alpha(i) / 255.0F);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class OminousSpawnProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public OminousSpawnProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			FlyStraightTowardsParticle flyStraightTowardsParticle = new FlyStraightTowardsParticle(clientLevel, d, e, f, g, h, i, -12210434, -1);
			flyStraightTowardsParticle.scale(Mth.randomBetween(clientLevel.getRandom(), 3.0F, 5.0F));
			flyStraightTowardsParticle.pickSprite(this.sprite);
			return flyStraightTowardsParticle;
		}
	}
}
