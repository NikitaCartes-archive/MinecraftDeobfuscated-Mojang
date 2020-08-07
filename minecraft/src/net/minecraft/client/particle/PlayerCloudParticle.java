package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class PlayerCloudParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	private PlayerCloudParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.sprites = spriteSet;
		float j = 2.5F;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.xd += g;
		this.yd += h;
		this.zd += i;
		float k = 1.0F - (float)(Math.random() * 0.3F);
		this.rCol = k;
		this.gCol = k;
		this.bCol = k;
		this.quadSize *= 1.875F;
		int l = (int)(8.0 / (Math.random() * 0.8 + 0.3));
		this.lifetime = (int)Math.max((float)l * 2.5F, 1.0F);
		this.hasPhysics = false;
		this.setSpriteFromAge(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
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
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.96F;
			this.yd *= 0.96F;
			this.zd *= 0.96F;
			Player player = this.level.getNearestPlayer(this.x, this.y, this.z, 2.0, false);
			if (player != null) {
				double d = player.getY();
				if (this.y > d) {
					this.y = this.y + (d - this.y) * 0.2;
					this.yd = this.yd + (player.getDeltaMovement().y - this.yd) * 0.2;
					this.setPos(this.x, this.y, this.z);
				}
			}

			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new PlayerCloudParticle(clientLevel, d, e, f, g, h, i, this.sprites);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SneezeProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public SneezeProvider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			Particle particle = new PlayerCloudParticle(clientLevel, d, e, f, g, h, i, this.sprites);
			particle.setColor(200.0F, 50.0F, 120.0F);
			particle.setAlpha(0.4F);
			return particle;
		}
	}
}
