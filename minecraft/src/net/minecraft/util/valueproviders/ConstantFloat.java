package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;

public class ConstantFloat extends FloatProvider {
	public static final ConstantFloat ZERO = new ConstantFloat(0.0F);
	public static final Codec<ConstantFloat> CODEC = Codec.either(
			Codec.FLOAT,
			RecordCodecBuilder.create(
				instance -> instance.group(Codec.FLOAT.fieldOf("value").forGetter(constantFloat -> constantFloat.value)).apply(instance, ConstantFloat::new)
			)
		)
		.xmap(either -> either.map(ConstantFloat::of, constantFloat -> constantFloat), constantFloat -> Either.left(constantFloat.value));
	private final float value;

	public static ConstantFloat of(float f) {
		return f == 0.0F ? ZERO : new ConstantFloat(f);
	}

	private ConstantFloat(float f) {
		this.value = f;
	}

	public float getValue() {
		return this.value;
	}

	@Override
	public float sample(Random random) {
		return this.value;
	}

	@Override
	public float getMinValue() {
		return this.value;
	}

	@Override
	public float getMaxValue() {
		return this.value + 1.0F;
	}

	@Override
	public FloatProviderType<?> getType() {
		return FloatProviderType.CONSTANT;
	}

	public String toString() {
		return Float.toString(this.value);
	}
}
