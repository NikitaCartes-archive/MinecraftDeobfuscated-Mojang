package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class VibrationSignalParticle extends TextureSheetParticle {
	private final PositionSource target;
	private float rot;
	private float rotO;
	private float pitch;
	private float pitchO;

	VibrationSignalParticle(ClientLevel clientLevel, double d, double e, double f, PositionSource positionSource, int i) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.quadSize = 0.3F;
		this.target = positionSource;
		this.lifetime = i;
		Optional<Vec3> optional = positionSource.getPosition(clientLevel);
		if (optional.isPresent()) {
			Vec3 vec3 = (Vec3)optional.get();
			double g = d - vec3.x();
			double h = e - vec3.y();
			double j = f - vec3.z();
			this.rotO = this.rot = (float)Mth.atan2(g, j);
			this.pitchO = this.pitch = (float)Mth.atan2(h, Math.sqrt(g * g + j * j));
		}
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		float g = Mth.sin(((float)this.age + f - (float) (Math.PI * 2)) * 0.05F) * 2.0F;
		float h = Mth.lerp(f, this.rotO, this.rot);
		float i = Mth.lerp(f, this.pitchO, this.pitch) + (float) (Math.PI / 2);
		this.renderSignal(vertexConsumer, camera, f, quaternion -> {
			quaternion.mul(Vector3f.YP.rotation(h));
			quaternion.mul(Vector3f.XP.rotation(-i));
			quaternion.mul(Vector3f.YP.rotation(g));
		});
		this.renderSignal(vertexConsumer, camera, f, quaternion -> {
			quaternion.mul(Vector3f.YP.rotation((float) -Math.PI + h));
			quaternion.mul(Vector3f.XP.rotation(i));
			quaternion.mul(Vector3f.YP.rotation(g));
		});
	}

	private void renderSignal(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternion> consumer) {
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
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			Optional<Vec3> optional = this.target.getPosition(this.level);
			if (optional.isEmpty()) {
				this.remove();
			} else {
				int i = this.lifetime - this.age;
				double d = 1.0 / (double)i;
				Vec3 vec3 = (Vec3)optional.get();
				this.x = Mth.lerp(d, this.x, vec3.x());
				this.y = Mth.lerp(d, this.y, vec3.y());
				this.z = Mth.lerp(d, this.z, vec3.z());
				double e = this.x - vec3.x();
				double f = this.y - vec3.y();
				double g = this.z - vec3.z();
				this.rotO = this.rot;
				this.rot = (float)Mth.atan2(e, g);
				this.pitchO = this.pitch;
				this.pitch = (float)Mth.atan2(f, Math.sqrt(e * e + g * g));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<VibrationParticleOption> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(
			VibrationParticleOption vibrationParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i
		) {
			VibrationSignalParticle vibrationSignalParticle = new VibrationSignalParticle(
				clientLevel, d, e, f, vibrationParticleOption.getDestination(), vibrationParticleOption.getArrivalInTicks()
			);
			vibrationSignalParticle.pickSprite(this.sprite);
			vibrationSignalParticle.setAlpha(1.0F);
			return vibrationSignalParticle;
		}
	}
}
