package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
	private static final float MAGICAL_X_ROT = 1.0472F;
	private int delay;

	ShriekParticle(ClientLevel clientLevel, double d, double e, double f, int i) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.quadSize = 0.85F;
		this.delay = i;
		this.lifetime = 30;
		this.gravity = 0.0F;
		this.xd = 0.0;
		this.yd = 0.1;
		this.zd = 0.0;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 0.75F, 0.0F, 1.0F);
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		if (this.delay <= 0) {
			this.alpha = 1.0F - Mth.clamp(((float)this.age + f) / (float)this.lifetime, 0.0F, 1.0F);
			Quaternionf quaternionf = new Quaternionf();
			quaternionf.rotationX(-1.0472F);
			this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
			quaternionf.rotationYXZ((float) -Math.PI, 1.0472F, 0.0F);
			this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
		}
	}

	@Override
	public int getLightColor(float f) {
		return 240;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		if (this.delay > 0) {
			this.delay--;
		} else {
			super.tick();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<ShriekParticleOption> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(ShriekParticleOption shriekParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			ShriekParticle shriekParticle = new ShriekParticle(clientLevel, d, e, f, shriekParticleOption.getDelay());
			shriekParticle.pickSprite(this.sprite);
			shriekParticle.setAlpha(1.0F);
			return shriekParticle;
		}
	}
}
