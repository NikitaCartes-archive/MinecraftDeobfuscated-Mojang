package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BaseAshSmokeParticle extends TextureSheetParticle {
	private final SpriteSet sprites;
	private final double fallSpeed;

	protected BaseAshSmokeParticle(
		ClientLevel clientLevel,
		double d,
		double e,
		double f,
		float g,
		float h,
		float i,
		double j,
		double k,
		double l,
		float m,
		SpriteSet spriteSet,
		float n,
		int o,
		double p,
		boolean bl
	) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.fallSpeed = p;
		this.sprites = spriteSet;
		this.xd *= (double)g;
		this.yd *= (double)h;
		this.zd *= (double)i;
		this.xd += j;
		this.yd += k;
		this.zd += l;
		float q = clientLevel.random.nextFloat() * n;
		this.rCol = q;
		this.gCol = q;
		this.bCol = q;
		this.quadSize *= 0.75F * m;
		this.lifetime = (int)((double)o / ((double)clientLevel.random.nextFloat() * 0.8 + 0.2));
		this.lifetime = (int)((float)this.lifetime * m);
		this.lifetime = Math.max(this.lifetime, 1);
		this.setSpriteFromAge(spriteSet);
		this.hasPhysics = bl;
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
			this.yd = this.yd + this.fallSpeed;
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
