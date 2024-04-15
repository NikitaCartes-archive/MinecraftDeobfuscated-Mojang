package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class DustParticleBase<T extends ScalableParticleOptionsBase> extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected DustParticleBase(
		ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, T scalableParticleOptionsBase, SpriteSet spriteSet
	) {
		super(clientLevel, d, e, f, g, h, i);
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = spriteSet;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.quadSize = this.quadSize * 0.75F * scalableParticleOptionsBase.getScale();
		int j = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
		this.lifetime = (int)Math.max((float)j * scalableParticleOptionsBase.getScale(), 1.0F);
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
