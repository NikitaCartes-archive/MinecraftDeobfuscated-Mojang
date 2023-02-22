package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

@Environment(EnvType.CLIENT)
public class BubbleParticle extends TextureSheetParticle {
	BubbleParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.xd = g * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.yd = h * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.zd = i * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.yd += 0.002;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;
			if (!this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
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

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			BubbleParticle bubbleParticle = new BubbleParticle(clientLevel, d, e, f, g, h, i);
			bubbleParticle.pickSprite(this.sprite);
			return bubbleParticle;
		}
	}
}
