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
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		float j = this.random.nextFloat() * 0.4F + 0.6F;
		this.rCol = this.randomizeColor(dustParticleOptionsBase.getColor().x(), j);
		this.gCol = this.randomizeColor(dustParticleOptionsBase.getColor().y(), j);
		this.bCol = this.randomizeColor(dustParticleOptionsBase.getColor().z(), j);
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
		super.tick();
		this.setSpriteFromAge(this.sprites);
	}
}
