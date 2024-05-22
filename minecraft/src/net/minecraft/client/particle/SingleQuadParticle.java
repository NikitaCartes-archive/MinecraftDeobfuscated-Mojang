package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class SingleQuadParticle extends Particle {
	protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

	protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f);
	}

	protected SingleQuadParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
	}

	public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
		return SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		Quaternionf quaternionf = new Quaternionf();
		this.getFacingCameraMode().setRotation(quaternionf, camera, f);
		if (this.roll != 0.0F) {
			quaternionf.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
		}

		this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
	}

	protected void renderRotatedQuad(VertexConsumer vertexConsumer, Camera camera, Quaternionf quaternionf, float f) {
		Vec3 vec3 = camera.getPosition();
		float g = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
		float h = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
		float i = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
		this.renderRotatedQuad(vertexConsumer, quaternionf, g, h, i, f);
	}

	protected void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float f, float g, float h, float i) {
		float j = this.getQuadSize(i);
		float k = this.getU0();
		float l = this.getU1();
		float m = this.getV0();
		float n = this.getV1();
		int o = this.getLightColor(i);
		this.renderVertex(vertexConsumer, quaternionf, f, g, h, 1.0F, -1.0F, j, l, n, o);
		this.renderVertex(vertexConsumer, quaternionf, f, g, h, 1.0F, 1.0F, j, l, m, o);
		this.renderVertex(vertexConsumer, quaternionf, f, g, h, -1.0F, 1.0F, j, k, m, o);
		this.renderVertex(vertexConsumer, quaternionf, f, g, h, -1.0F, -1.0F, j, k, n, o);
	}

	private void renderVertex(
		VertexConsumer vertexConsumer, Quaternionf quaternionf, float f, float g, float h, float i, float j, float k, float l, float m, int n
	) {
		Vector3f vector3f = new Vector3f(i, j, 0.0F).rotate(quaternionf).mul(k).add(f, g, h);
		vertexConsumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(l, m).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(n);
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

	@Environment(EnvType.CLIENT)
	public interface FacingCameraMode {
		SingleQuadParticle.FacingCameraMode LOOKAT_XYZ = (quaternionf, camera, f) -> quaternionf.set(camera.rotation());
		SingleQuadParticle.FacingCameraMode LOOKAT_Y = (quaternionf, camera, f) -> quaternionf.set(0.0F, camera.rotation().y, 0.0F, camera.rotation().w);

		void setRotation(Quaternionf quaternionf, Camera camera, float f);
	}
}
