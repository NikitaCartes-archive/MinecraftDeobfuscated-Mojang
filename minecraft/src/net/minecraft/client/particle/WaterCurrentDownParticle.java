package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WaterCurrentDownParticle extends TextureSheetParticle {
	private float angle;

	WaterCurrentDownParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f);
		this.lifetime = (int)(Math.random() * 60.0) + 30;
		this.hasPhysics = false;
		this.xd = 0.0;
		this.yd = -0.05;
		this.zd = 0.0;
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.gravity = 0.002F;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			float f = 0.6F;
			this.xd = this.xd + (double)(0.6F * Mth.cos(this.angle));
			this.zd = this.zd + (double)(0.6F * Mth.sin(this.angle));
			this.xd *= 0.07;
			this.zd *= 0.07;
			this.move(this.xd, this.yd, this.zd);
			if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER) || this.onGround) {
				this.remove();
			}

			this.angle = (float)((double)this.angle + 0.08);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			WaterCurrentDownParticle waterCurrentDownParticle = new WaterCurrentDownParticle(clientLevel, d, e, f);
			waterCurrentDownParticle.pickSprite(this.sprite);
			return waterCurrentDownParticle;
		}
	}
}
