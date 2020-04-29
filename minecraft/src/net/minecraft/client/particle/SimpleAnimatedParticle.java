package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
public class SimpleAnimatedParticle extends TextureSheetParticle {
	protected final SpriteSet sprites;
	private final float baseGravity;
	private float baseAirFriction = 0.91F;
	private float fadeR;
	private float fadeG;
	private float fadeB;
	private boolean hasFade;

	protected SimpleAnimatedParticle(ClientLevel clientLevel, double d, double e, double f, SpriteSet spriteSet, float g) {
		super(clientLevel, d, e, f);
		this.sprites = spriteSet;
		this.baseGravity = g;
	}

	public void setColor(int i) {
		float f = (float)((i & 0xFF0000) >> 16) / 255.0F;
		float g = (float)((i & 0xFF00) >> 8) / 255.0F;
		float h = (float)((i & 0xFF) >> 0) / 255.0F;
		float j = 1.0F;
		this.setColor(f * 1.0F, g * 1.0F, h * 1.0F);
	}

	public void setFadeColor(int i) {
		this.fadeR = (float)((i & 0xFF0000) >> 16) / 255.0F;
		this.fadeG = (float)((i & 0xFF00) >> 8) / 255.0F;
		this.fadeB = (float)((i & 0xFF) >> 0) / 255.0F;
		this.hasFade = true;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
				if (this.hasFade) {
					this.rCol = this.rCol + (this.fadeR - this.rCol) * 0.2F;
					this.gCol = this.gCol + (this.fadeG - this.gCol) * 0.2F;
					this.bCol = this.bCol + (this.fadeB - this.bCol) * 0.2F;
				}
			}

			this.yd = this.yd + (double)this.baseGravity;
			this.move(this.xd, this.yd, this.zd);
			this.xd = this.xd * (double)this.baseAirFriction;
			this.yd = this.yd * (double)this.baseAirFriction;
			this.zd = this.zd * (double)this.baseAirFriction;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}

	@Override
	public int getLightColor(float f) {
		return 15728880;
	}

	protected void setBaseAirFriction(float f) {
		this.baseAirFriction = f;
	}
}
