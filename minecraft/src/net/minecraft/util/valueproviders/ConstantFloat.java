package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public class ConstantFloat extends FloatProvider {
	public static final ConstantFloat ZERO = new ConstantFloat(0.0F);
	public static final Codec<ConstantFloat> CODEC = ExtraCodecs.withAlternative(Codec.FLOAT, Codec.FLOAT.fieldOf("value").codec())
		.xmap(ConstantFloat::new, ConstantFloat::getValue);
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
	public float sample(RandomSource randomSource) {
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
