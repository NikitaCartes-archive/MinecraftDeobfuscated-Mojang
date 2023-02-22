package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

@Environment(EnvType.CLIENT)
public class BubbleColumnUpParticle extends TextureSheetParticle {
	BubbleColumnUpParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.gravity = -0.125F;
		this.friction = 0.85F;
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.xd = g * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.yd = h * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.zd = i * 0.2F + (Math.random() * 2.0 - 1.0) * 0.02F;
		this.lifetime = (int)(40.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.removed && !this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
			this.remove();
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
			BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(clientLevel, d, e, f, g, h, i);
			bubbleColumnUpParticle.pickSprite(this.sprite);
			return bubbleColumnUpParticle;
		}
	}
}
