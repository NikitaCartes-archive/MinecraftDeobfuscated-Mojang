package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
	private static final Vector3f ROTATION_VECTOR = new Vector3f(0.5F, 0.5F, 0.5F).normalize();
	private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
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
			this.renderRotatedParticle(vertexConsumer, camera, f, quaternionf -> quaternionf.mul(new Quaternionf().rotationX(-1.0472F)));
			this.renderRotatedParticle(vertexConsumer, camera, f, quaternionf -> quaternionf.mul(new Quaternionf().rotationYXZ((float) -Math.PI, 1.0472F, 0.0F)));
		}
	}

	private void renderRotatedParticle(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternionf> consumer) {
		Vec3 vec3 = camera.getPosition();
		float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
		float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
		float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
		Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
		consumer.accept(quaternionf);
		quaternionf.transform(TRANSFORM_VECTOR);
		Vector3f[] vector3fs = new Vector3f[]{
			new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
		};
		float j = this.getQuadSize(f);

		for (int k = 0; k < 4; k++) {
			Vector3f vector3f = vector3fs[k];
			vector3f.rotate(quaternionf);
			vector3f.mul(j);
			vector3f.add(g, h, i);
		}

		int k = this.getLightColor(f);
		this.makeCornerVertex(vertexConsumer, vector3fs[0], this.getU1(), this.getV1(), k);
		this.makeCornerVertex(vertexConsumer, vector3fs[1], this.getU1(), this.getV0(), k);
		this.makeCornerVertex(vertexConsumer, vector3fs[2], this.getU0(), this.getV0(), k);
		this.makeCornerVertex(vertexConsumer, vector3fs[3], this.getU0(), this.getV1(), k);
	}

	private void makeCornerVertex(VertexConsumer vertexConsumer, Vector3f vector3f, float f, float g, int i) {
		vertexConsumer.vertex((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z())
			.uv(f, g)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(i)
			.endVertex();
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
