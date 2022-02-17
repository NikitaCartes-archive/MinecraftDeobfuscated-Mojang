package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
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
			float g = 1.0472F;
			this.renderRotatedParticle(vertexConsumer, camera, f, quaternion -> {
				quaternion.mul(Vector3f.YP.rotation(0.0F));
				quaternion.mul(Vector3f.XP.rotation(-1.0472F));
			});
			this.renderRotatedParticle(vertexConsumer, camera, f, quaternion -> {
				quaternion.mul(Vector3f.YP.rotation((float) -Math.PI));
				quaternion.mul(Vector3f.XP.rotation(1.0472F));
			});
		}
	}

	private void renderRotatedParticle(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternion> consumer) {
		Vec3 vec3 = camera.getPosition();
		float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
		float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
		float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
		Vector3f vector3f = new Vector3f(0.5F, 0.5F, 0.5F);
		vector3f.normalize();
		Quaternion quaternion = new Quaternion(vector3f, 0.0F, true);
		consumer.accept(quaternion);
		Vector3f vector3f2 = new Vector3f(-1.0F, -1.0F, 0.0F);
		vector3f2.transform(quaternion);
		Vector3f[] vector3fs = new Vector3f[]{
			new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
		};
		float j = this.getQuadSize(f);

		for (int k = 0; k < 4; k++) {
			Vector3f vector3f3 = vector3fs[k];
			vector3f3.transform(quaternion);
			vector3f3.mul(j);
			vector3f3.add(g, h, i);
		}

		float l = this.getU0();
		float m = this.getU1();
		float n = this.getV0();
		float o = this.getV1();
		int p = this.getLightColor(f);
		vertexConsumer.vertex((double)vector3fs[0].x(), (double)vector3fs[0].y(), (double)vector3fs[0].z())
			.uv(m, o)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(p)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[1].x(), (double)vector3fs[1].y(), (double)vector3fs[1].z())
			.uv(m, n)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(p)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[2].x(), (double)vector3fs[2].y(), (double)vector3fs[2].z())
			.uv(l, n)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(p)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[3].x(), (double)vector3fs[3].y(), (double)vector3fs[3].z())
			.uv(l, o)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(p)
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
