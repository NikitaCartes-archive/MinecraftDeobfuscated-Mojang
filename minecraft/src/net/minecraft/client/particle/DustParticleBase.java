package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DustParticleBase<T extends DustParticleOptionsBase> extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected DustParticleBase(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, T dustParticleOptionsBase, SpriteSet spriteSet) {
		super(clientLevel, d, e, f, g, h, i);
		this.sprites = spriteSet;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		float j = this.random.nextFloat() * 0.4F + 0.6F;
		this.rCol = this.randomizeColor((float)dustParticleOptionsBase.getColor().x, j);
		this.gCol = this.randomizeColor((float)dustParticleOptionsBase.getColor().y, j);
		this.bCol = this.randomizeColor((float)dustParticleOptionsBase.getColor().z, j);
		this.quadSize = this.quadSize * 0.75F * dustParticleOptionsBase.getScale();
		int k = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
		this.lifetime = (int)Math.max((float)k * dustParticleOptionsBase.getScale(), 1.0F);
		this.setSpriteFromAge(spriteSet);
	}

	protected float randomizeColor(float f, float g) {
		return (this.random.nextFloat() * 0.2F + 0.8F) * f * g;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
			if (this.y == this.yo) {
				this.xd *= 1.1;
				this.zd *= 1.1;
			}

			this.xd *= 0.96F;
			this.yd *= 0.96F;
			this.zd *= 0.96F;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}
}
