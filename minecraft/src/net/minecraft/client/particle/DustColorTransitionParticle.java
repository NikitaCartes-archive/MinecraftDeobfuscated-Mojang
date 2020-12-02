package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
	private final Vec3 fromColor;
	private final Vec3 toColor;

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

	private Vec3 randomizeColor(Vec3 vec3, float f) {
		return new Vec3((double)this.randomizeColor((float)vec3.x, f), (double)this.randomizeColor((float)vec3.y, f), (double)this.randomizeColor((float)vec3.z, f));
	}

	private void lerpColors(float f) {
		float g = ((float)this.age + f) / ((float)this.lifetime + 1.0F);
		Vec3 vec3 = this.fromColor.lerp(this.toColor, (double)g);
		this.rCol = (float)vec3.x;
		this.gCol = (float)vec3.y;
		this.bCol = (float)vec3.z;
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
