package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class SingleQuadParticle extends Particle {
	protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

	protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f);
	}

	protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		Vec3 vec3 = camera.getPosition();
		float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
		float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
		float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
		Quaternion quaternion;
		if (this.roll == 0.0F) {
			quaternion = camera.rotation();
		} else {
			quaternion = new Quaternion(camera.rotation());
			float j = Mth.lerp(f, this.oRoll, this.roll);
			quaternion.mul(Vector3f.ZP.rotation(j));
		}

		Vector3f vector3f = new Vector3f(-1.0F, -1.0F, 0.0F);
		vector3f.transform(quaternion);
		Vector3f[] vector3fs = new Vector3f[]{
			new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
		};
		float k = this.getQuadSize(f);

		for (int l = 0; l < 4; l++) {
			Vector3f vector3f2 = vector3fs[l];
			vector3f2.transform(quaternion);
			vector3f2.mul(k);
			vector3f2.add(g, h, i);
		}

		float m = this.getU0();
		float n = this.getU1();
		float o = this.getV0();
		float p = this.getV1();
		int q = this.getLightColor(f);
		vertexConsumer.vertex((double)vector3fs[0].x(), (double)vector3fs[0].y(), (double)vector3fs[0].z())
			.uv(n, p)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(q)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[1].x(), (double)vector3fs[1].y(), (double)vector3fs[1].z())
			.uv(n, o)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(q)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[2].x(), (double)vector3fs[2].y(), (double)vector3fs[2].z())
			.uv(m, o)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(q)
			.endVertex();
		vertexConsumer.vertex((double)vector3fs[3].x(), (double)vector3fs[3].y(), (double)vector3fs[3].z())
			.uv(m, p)
			.color(this.rCol, this.gCol, this.bCol, this.alpha)
			.uv2(q)
			.endVertex();
	}

	public float getQuadSize(float f) {
		return this.quadSize;
	}

	@Override
	public Particle scale(float f) {
		this.quadSize *= f;
		return super.scale(f);
	}

	protected abstract float getU0();

	protected abstract float getU1();

	protected abstract float getV0();

	protected abstract float getV1();
}
