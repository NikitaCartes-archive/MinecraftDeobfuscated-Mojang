package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class BubbleColumnUpParticle extends TextureSheetParticle {
	private BubbleColumnUpParticle(Level level, double d, double e, double f, double g, double h, double i) {
		super(level, d, e, f);
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.xd = g * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.yd = h * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.zd = i * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.lifetime = (int)(40.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd += 0.005;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;
			if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)) {
				this.remove();
			}
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(level, d, e, f, g, h, i);
			bubbleColumnUpParticle.pickSprite(this.sprite);
			return bubbleColumnUpParticle;
		}
	}
}
