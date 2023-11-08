package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class GustDustParticle extends TextureSheetParticle {
	private final Vector3f fromColor = new Vector3f(0.5F, 0.5F, 0.5F);
	private final Vector3f toColor = new Vector3f(1.0F, 1.0F, 1.0F);

	GustDustParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f);
		this.hasPhysics = false;
		this.xd = g + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
		this.zd = i + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
		double j = Math.random() * 2.0;
		double k = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
		this.xd = this.xd / k * j * 0.4F;
		this.zd = this.zd / k * j * 0.4F;
		this.quadSize *= 2.5F;
		this.xd *= 0.08F;
		this.zd *= 0.08F;
		this.lifetime = 18 + this.random.nextInt(4);
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		this.lerpColors(f);
		super.render(vertexConsumer, camera, f);
	}

	private void lerpColors(float f) {
		float g = ((float)this.age + f) / (float)(this.lifetime + 1);
		Vector3f vector3f = new Vector3f(this.fromColor).lerp(this.toColor, g);
		this.rCol = vector3f.x();
		this.gCol = vector3f.y();
		this.bCol = vector3f.z();
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.xo = this.x;
			this.zo = this.z;
			this.move(this.xd, 0.0, this.zd);
			this.xd *= 0.99;
			this.zd *= 0.99;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class GustDustParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public GustDustParticleProvider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			GustDustParticle gustDustParticle = new GustDustParticle(clientLevel, d, e, f, g, h, i);
			gustDustParticle.pickSprite(this.sprite);
			return gustDustParticle;
		}
	}
}
