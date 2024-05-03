package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

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
		Quaternionf quaternionf = new Quaternionf();
		quaternionf.rotationY(h).rotateX(-i).rotateY(g);
		this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
		quaternionf.rotationY((float) -Math.PI + h).rotateX(i).rotateY(g);
		this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
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
