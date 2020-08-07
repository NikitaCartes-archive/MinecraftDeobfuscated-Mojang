package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class DripParticle extends TextureSheetParticle {
	private final Fluid type;
	protected boolean isGlowing;

	private DripParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid) {
		super(clientLevel, d, e, f);
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
		return this.isGlowing ? 240 : super.getLightColor(f);
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
		private CoolingDripHangParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(clientLevel, d, e, f, fluid, particleOptions);
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
	static class DripHangParticle extends DripParticle {
		private final ParticleOptions fallingParticle;

		private DripHangParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(clientLevel, d, e, f, fluid);
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
		private DripLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid) {
			super(clientLevel, d, e, f, fluid);
			this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
		}
	}

	@Environment(EnvType.CLIENT)
	static class FallAndLandParticle extends DripParticle.FallingParticle {
		protected final ParticleOptions landParticle;

		private FallAndLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(clientLevel, d, e, f, fluid);
			this.landParticle = particleOptions;
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
	static class FallingParticle extends DripParticle {
		private FallingParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid) {
			super(clientLevel, d, e, f, fluid);
			this.lifetime = (int)(64.0 / (Math.random() * 0.8 + 0.2));
		}

		@Override
		protected void postMoveUpdate() {
			if (this.onGround) {
				this.remove();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class HoneyFallAndLandParticle extends DripParticle.FallAndLandParticle {
		private HoneyFallAndLandParticle(ClientLevel clientLevel, double d, double e, double f, Fluid fluid, ParticleOptions particleOptions) {
			super(clientLevel, d, e, f, fluid, particleOptions);
		}

		@Override
		protected void postMoveUpdate() {
			if (this.onGround) {
				this.remove();
				this.level.addParticle(this.landParticle, this.x, this.y, this.z, 0.0, 0.0, 0.0);
				this.level
					.playLocalSound(
						this.x + 0.5, this.y, this.z + 0.5, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, 0.3F + this.level.random.nextFloat() * 2.0F / 3.0F, 1.0F, false
					);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class HoneyFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public HoneyFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.HoneyFallAndLandParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_HONEY);
			dripParticle.gravity = 0.01F;
			dripParticle.setColor(0.582F, 0.448F, 0.082F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class HoneyHangProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public HoneyHangProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle.DripHangParticle dripHangParticle = new DripParticle.DripHangParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_HONEY);
			dripHangParticle.gravity *= 0.01F;
			dripHangParticle.lifetime = 100;
			dripHangParticle.setColor(0.622F, 0.508F, 0.082F);
			dripHangParticle.pickSprite(this.sprite);
			return dripHangParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class HoneyLandProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public HoneyLandProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripLandParticle(clientLevel, d, e, f, Fluids.EMPTY);
			dripParticle.lifetime = (int)(128.0 / (Math.random() * 0.8 + 0.2));
			dripParticle.setColor(0.522F, 0.408F, 0.082F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LavaFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public LavaFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.FallAndLandParticle(clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.LANDING_LAVA);
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle.CoolingDripHangParticle coolingDripHangParticle = new DripParticle.CoolingDripHangParticle(
				clientLevel, d, e, f, Fluids.LAVA, ParticleTypes.FALLING_LAVA
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripLandParticle(clientLevel, d, e, f, Fluids.LAVA);
			dripParticle.setColor(1.0F, 0.2857143F, 0.083333336F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class NectarFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public NectarFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.FallingParticle(clientLevel, d, e, f, Fluids.EMPTY);
			dripParticle.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
			dripParticle.gravity = 0.007F;
			dripParticle.setColor(0.92F, 0.782F, 0.72F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ObsidianTearFallProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public ObsidianTearFallProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.FallAndLandParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.LANDING_OBSIDIAN_TEAR);
			dripParticle.isGlowing = true;
			dripParticle.gravity = 0.01F;
			dripParticle.setColor(0.51171875F, 0.03125F, 0.890625F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ObsidianTearHangProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public ObsidianTearHangProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle.DripHangParticle dripHangParticle = new DripParticle.DripHangParticle(clientLevel, d, e, f, Fluids.EMPTY, ParticleTypes.FALLING_OBSIDIAN_TEAR);
			dripHangParticle.isGlowing = true;
			dripHangParticle.gravity *= 0.01F;
			dripHangParticle.lifetime = 100;
			dripHangParticle.setColor(0.51171875F, 0.03125F, 0.890625F);
			dripHangParticle.pickSprite(this.sprite);
			return dripHangParticle;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ObsidianTearLandProvider implements ParticleProvider<SimpleParticleType> {
		protected final SpriteSet sprite;

		public ObsidianTearLandProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripLandParticle(clientLevel, d, e, f, Fluids.EMPTY);
			dripParticle.isGlowing = true;
			dripParticle.lifetime = (int)(28.0 / (Math.random() * 0.8 + 0.2));
			dripParticle.setColor(0.51171875F, 0.03125F, 0.890625F);
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.FallAndLandParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.SPLASH);
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			DripParticle dripParticle = new DripParticle.DripHangParticle(clientLevel, d, e, f, Fluids.WATER, ParticleTypes.FALLING_WATER);
			dripParticle.setColor(0.2F, 0.3F, 1.0F);
			dripParticle.pickSprite(this.sprite);
			return dripParticle;
		}
	}
}
