package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public abstract class ScalableParticleOptionsBase implements ParticleOptions {
	public static final float MIN_SCALE = 0.01F;
	public static final float MAX_SCALE = 4.0F;
	protected static final Codec<Float> SCALE = Codec.FLOAT
		.validate(
			float_ -> float_ >= 0.01F && float_ <= 4.0F ? DataResult.success(float_) : DataResult.error(() -> "Value must be within range [0.01;4.0]: " + float_)
		);
	private final float scale;

	public ScalableParticleOptionsBase(float f) {
		this.scale = Mth.clamp(f, 0.01F, 4.0F);
	}

	public float getScale() {
		return this.scale;
	}
}
