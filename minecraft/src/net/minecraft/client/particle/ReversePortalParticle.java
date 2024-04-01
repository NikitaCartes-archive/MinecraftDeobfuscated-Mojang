package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class ReversePortalParticle extends PortalParticle {
	ReversePortalParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
		this.quadSize *= 1.5F;
		this.lifetime = (int)(Math.random() * 2.0) + 60;
	}

	@Override
	public float getQuadSize(float f) {
		float g = 1.0F - ((float)this.age + f) / ((float)this.lifetime * 1.5F);
		return this.quadSize * g;
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
			this.x = this.x + this.xd * (double)f;
			this.y = this.y + this.yd * (double)f;
			this.z = this.z + this.zd * (double)f;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ReversePortalProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;
		private final float rCol;
		private final float gCol;
		private final float bCol;

		public ReversePortalProvider(SpriteSet spriteSet, float f, float g, float h) {
			this.sprite = spriteSet;
			this.rCol = f;
			this.gCol = g;
			this.bCol = h;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			ReversePortalParticle reversePortalParticle = new ReversePortalParticle(clientLevel, d, e, f, g, h, i);
			float j = clientLevel.random.nextFloat() * 0.6F + 0.4F;
			reversePortalParticle.setColor(this.rCol * j, this.gCol * j, this.bCol * j);
			reversePortalParticle.pickSprite(this.sprite);
			return reversePortalParticle;
		}
	}
}
