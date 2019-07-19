package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class DripParticle extends TextureSheetParticle {
	private final Fluid type;

	private DripParticle(Level level, double d, double e, double f, Fluid fluid) {
		super(level, d, e, f);
		this.setSize(0.01F, 0.01F);
		this.gravity = 0.06F;
		this.type = fluid;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public int getLightColor(float f) {
		return this.type.is(FluidTags.LAVA) ? 240 : super.getLightColor(f);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.preMoveUpdate();
		if (!this.removed) {
			this.yd = this.yd - (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			this.postMoveUpdate();
			if (!this.removed) {
				this.xd *= 0.98F;
				this.yd *= 0.98F;
				this.zd *= 0.98F;
				BlockPos blockPos = new BlockPos(this.x, this.y, this.z);
				FluidState fluidState = this.level.getFluidState(blockPos);
				if (fluidState.getType() == this.type && this.y < (double)((float)blockPos.getY() + fluidState.getHeight(this.level, blockPos))) {
					this.remove();
				}
			}
		}
	}

	protected void preMoveUpdate() {
		if (this.lifetime-- <= 0) {
			this.remove();
		}
	}

	protected void postMoveUpdate() {
	}

	@Environment(EnvType.CLIENT)
	static class CoolingDripHangParticle extends DripParticle.DripHangParticle {
		private CoolingDripHangParticle(Level level, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(level, d, e, f, fluid, particleOptions);
		}

		@Override
		protected void preMoveUpdate() {
			this.rCol = 1.0F;
			this.gCol = 16.0F / (float)(40 - this.lifetime + 16);
			this.bCol = 4.0F / (float)(40 - this.lifetime + 8);
			super.preMoveUpdate();
		}
	}

	@Environment(EnvType.CLIENT)
	static class DripFallParticle extends DripParticle {
		private final ParticleOptions landParticle;

		private DripFallParticle(Level level, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(level, d, e, f, fluid);
			this.landParticle = particleOptions;
			this.lifetime = (int)(64.0 / (Math.random() * 0.8 + 0.2));
		}

		@Override
		protected void postMoveUpdate() {
			if (this.onGround) {
				this.remove();
				this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class DripHangParticle extends DripParticle {
		private final ParticleOptions fallingParticle;

		private DripHangParticle(Level level, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(level, d, e, f, fluid);
			this.fallingParticle = particleOptions;
			this.gravity *= 0.02F;
			this.lifetime = 40;
		}

		@Override
		protected void preMoveUpdate() {
			if (this.lifetime-- <= 0) {
				this.remove();
				this.level.addParticle(this.fallingParticle, this.x, this.y, this.z, this.xd, this.yd, this.zd);
			}
		}

		@Override
		protected void postMoveUpdate() {
			this.xd *= 0.02;
			this.yd *= 0.02;
			this.zd *= 0.02;
		}
	}

	@Environment(EnvType.CLIENT)
	static class DripLandParticle extends DripParticle {
		private DripLandParticle(Level level, double d, double e, double f, Fluid fluid) {
			super(level, d, e, f, fluid);
			this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LavaFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public LavaFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripFallParticle(level, d, e, f, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
			dripParticle.setColor(1.0F, 0.2857143F, 0.083333336F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LavaHangProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public LavaHangProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			DripParticle.CoolingDripHangParticle coolingDripHangParticle = new DripParticle.CoolingDripHangParticle(
				level, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_LAVA
			);
			coolingDripHangParticle.pickSprite(this.sprite);
			return coolingDripHangParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LavaLandProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public LavaLandProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripLandParticle(level, d, e, f, Fluids.LAVA);
			dripParticle.setColor(1.0F, 0.2857143F, 0.083333336F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WaterFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public WaterFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripFallParticle(level, d, e, f, Fluids.WATER, ParticleTypes.SPLASH);
			dripParticle.setColor(0.2F, 0.3F, 1.0F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WaterHangProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public WaterHangProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripHangParticle(level, d, e, f, Fluids.WATER, ParticleTypes.FALLING_WATER);
			dripParticle.setColor(0.2F, 0.3F, 1.0F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}
}
