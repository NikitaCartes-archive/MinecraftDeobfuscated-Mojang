package net.minecraft.network.protocol.game;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.VisibleForTesting;

public class VecDeltaCodec {
	private static final double TRUNCATION_STEPS = 4096.0;
	private Vec3 base = Vec3.ZERO;

	@VisibleForTesting
	static long encode(double d) {
		return Math.round(d * 4096.0);
	}

	@VisibleForTesting
	static double decode(long l) {
		return (double)l / 4096.0;
	}

	public Vec3 decode(long l, long m, long n) {
		if (l == 0L && m == 0L && n == 0L) {
			return this.base;
		} else {
			double d = l == 0L ? this.base.x : decode(encode(this.base.x) + l);
			double e = m == 0L ? this.base.y : decode(encode(this.base.y) + m);
			double f = n == 0L ? this.base.z : decode(encode(this.base.z) + n);
			return new Vec3(d, e, f);
		}
	}

	public long encodeX(Vec3 vec3) {
		return encode(vec3.x) - encode(this.base.x);
	}

	public long encodeY(Vec3 vec3) {
		return encode(vec3.y) - encode(this.base.y);
	}

	public long encodeZ(Vec3 vec3) {
		return encode(vec3.z) - encode(this.base.z);
	}

	public Vec3 delta(Vec3 vec3) {
		return vec3.subtract(this.base);
	}

	public void setBase(Vec3 vec3) {
		this.base = vec3;
	}
}
