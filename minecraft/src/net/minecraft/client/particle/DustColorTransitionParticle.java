package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
	private final Vector3f fromColor;
	private final Vector3f toColor;

	protected DustColorTransitionParticle(
		ClientLevel clientLevel,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		DustColorTransitionOptions dustColorTransitionOptions,
		SpriteSet spriteSet
	) {
		super(clientLevel, d, e, f, g, h, i, dustColorTransitionOptions, spriteSet);
		float j = this.random.nextFloat() * 0.4F + 0.6F;
		this.fromColor = this.randomizeColor(dustColorTransitionOptions.getFromColor(), j);
		this.toColor = this.randomizeColor(dustColorTransitionOptions.getToColor(), j);
	}

	private Vector3f randomizeColor(Vector3f vector3f, float f) {
		return new Vector3f(this.randomizeColor(vector3f.x(), f), this.randomizeColor(vector3f.y(), f), this.randomizeColor(vector3f.z(), f));
	}

	private void lerpColors(float f) {
		float g = ((float)this.age + f) / ((float)this.lifetime + 1.0F);
		Vector3f vector3f = new Vector3f(this.fromColor).lerp(this.toColor, g);
		this.rCol = vector3f.x();
		this.gCol = vector3f.y();
		this.bCol = vector3f.z();
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		this.lerpColors(f);
		super.render(vertexConsumer, camera, f);
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<DustColorTransitionOptions> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		public Particle createParticle(
			DustColorTransitionOptions dustColorTransitionOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i
		) {
			return new DustColorTransitionParticle(clientLevel, d, e, f, g, h, i, dustColorTransitionOptions, this.sprites);
		}
	}
}
