package net.minecraft.client.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public record AnimationChannel(AnimationChannel.Target target, Keyframe... keyframes) {
	@Environment(EnvType.CLIENT)
	public interface Interpolation {
		Vector3f apply(Vector3f vector3f, float f, Keyframe[] keyframes, int i, int j, float g);
	}

	@Environment(EnvType.CLIENT)
	public static class Interpolations {
		public static final AnimationChannel.Interpolation LINEAR = (vector3f, f, keyframes, i, j, g) -> {
			Vector3f vector3f2 = keyframes[i].target();
			Vector3f vector3f3 = keyframes[j].target();
			return vector3f2.lerp(vector3f3, f, vector3f).mul(g);
		};
		public static final AnimationChannel.Interpolation CATMULLROM = (vector3f, f, keyframes, i, j, g) -> {
			Vector3f vector3f2 = keyframes[Math.max(0, i - 1)].target();
			Vector3f vector3f3 = keyframes[i].target();
			Vector3f vector3f4 = keyframes[j].target();
			Vector3f vector3f5 = keyframes[Math.min(keyframes.length - 1, j + 1)].target();
			vector3f.set(
				Mth.catmullrom(f, vector3f2.x(), vector3f3.x(), vector3f4.x(), vector3f5.x()) * g,
				Mth.catmullrom(f, vector3f2.y(), vector3f3.y(), vector3f4.y(), vector3f5.y()) * g,
				Mth.catmullrom(f, vector3f2.z(), vector3f3.z(), vector3f4.z(), vector3f5.z()) * g
			);
			return vector3f;
		};
	}

	@Environment(EnvType.CLIENT)
	public interface Target {
		void apply(ModelPart modelPart, Vector3f vector3f);
	}

	@Environment(EnvType.CLIENT)
	public static class Targets {
		public static final AnimationChannel.Target POSITION = ModelPart::offsetPos;
		public static final AnimationChannel.Target ROTATION = ModelPart::offsetRotation;
		public static final AnimationChannel.Target SCALE = ModelPart::offsetScale;
	}
}
