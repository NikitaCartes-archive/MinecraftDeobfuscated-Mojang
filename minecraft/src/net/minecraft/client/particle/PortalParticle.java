package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class PortalParticle extends TextureSheetParticle {
	private final double xStart;
	private final double yStart;
	private final double zStart;

	protected PortalParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.xd = g;
		this.yd = h;
		this.zd = i;
		this.x = d;
		this.y = e;
		this.z = f;
		this.xStart = this.x;
		this.yStart = this.y;
		this.zStart = this.z;
		this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
		float j = this.random.nextFloat() * 0.6F + 0.4F;
		this.rCol = j * 0.9F;
		this.gCol = j * 0.3F;
		this.bCol = j;
		this.lifetime = (int)(Math.random() * 10.0) + 40;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void move(double d, double e, double f) {
		this.setBoundingBox(this.getBoundingBox().move(d, e, f));
		this.setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		g = 1.0F - g;
		g *= g;
		g = 1.0F - g;
		return this.quadSize * g;
	}

	@Override
	public int getLightColor(float f) {
		int i = super.getLightColor(f);
		float g = (float)this.age / (float)this.lifetime;
		g *= g;
		g *= g;
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		k += (int)(g * 15.0F * 16.0F);
		if (k > 240) {
			k = 240;
		}

		return j | k << 16;
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
			float var3 = -f + f * f * 2.0F;
			float var4 = 1.0F - var3;
			this.x = this.xStart + this.xd * (double)var4;
			this.y = this.yStart + this.yd * (double)var4 + (double)(1.0F - f);
			this.z = this.zStart + this.zd * (double)var4;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			PortalParticle portalParticle = new PortalParticle(clientLevel, d, e, f, g, h, i);
			portalParticle.pickSprite(this.sprite);
			return portalParticle;
		}
	}
}
