package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class LongJumpUtil {
	public static Optional<Vec3> calculateJumpVectorForAngle(Mob mob, Vec3 vec3, float f, int i, boolean bl) {
		Vec3 vec32 = mob.position();
		Vec3 vec33 = new Vec3(vec3.x - vec32.x, 0.0, vec3.z - vec32.z).normalize().scale(0.5);
		Vec3 vec34 = vec3.subtract(vec33);
		Vec3 vec35 = vec34.subtract(vec32);
		float g = (float)i * (float) Math.PI / 180.0F;
		double d = Math.atan2(vec35.z, vec35.x);
		double e = vec35.subtract(0.0, vec35.y, 0.0).lengthSqr();
		double h = Math.sqrt(e);
		double j = vec35.y;
		double k = mob.getGravity();
		double l = Math.sin((double)(2.0F * g));
		double m = Math.pow(Math.cos((double)g), 2.0);
		double n = Math.sin((double)g);
		double o = Math.cos((double)g);
		double p = Math.sin(d);
		double q = Math.cos(d);
		double r = e * k / (h * l - 2.0 * j * m);
		if (r < 0.0) {
			return Optional.empty();
		} else {
			double s = Math.sqrt(r);
			if (s > (double)f) {
				return Optional.empty();
			} else {
				double t = s * o;
				double u = s * n;
				if (bl) {
					int v = Mth.ceil(h / t) * 2;
					double w = 0.0;
					Vec3 vec36 = null;
					EntityDimensions entityDimensions = mob.getDimensions(Pose.LONG_JUMPING);

					for (int x = 0; x < v - 1; x++) {
						w += h / (double)v;
						double y = n / o * w - Math.pow(w, 2.0) * k / (2.0 * r * Math.pow(o, 2.0));
						double z = w * q;
						double aa = w * p;
						Vec3 vec37 = new Vec3(vec32.x + z, vec32.y + y, vec32.z + aa);
						if (vec36 != null && !isClearTransition(mob, entityDimensions, vec36, vec37)) {
							return Optional.empty();
						}

						vec36 = vec37;
					}
				}

				return Optional.of(new Vec3(t * q, u, t * p).scale(0.95F));
			}
		}
	}

	private static boolean isClearTransition(Mob mob, EntityDimensions entityDimensions, Vec3 vec3, Vec3 vec32) {
		Vec3 vec33 = vec32.subtract(vec3);
		double d = (double)Math.min(entityDimensions.width(), entityDimensions.height());
		int i = Mth.ceil(vec33.length() / d);
		Vec3 vec34 = vec33.normalize();
		Vec3 vec35 = vec3;

		for (int j = 0; j < i; j++) {
			vec35 = j == i - 1 ? vec32 : vec35.add(vec34.scale(d * 0.9F));
			if (!mob.level().noCollision(mob, entityDimensions.makeBoundingBox(vec35))) {
				return false;
			}
		}

		return true;
	}
}
