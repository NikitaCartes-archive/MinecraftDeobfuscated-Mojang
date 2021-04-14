package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
	public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
	public static final Codec<ConstantHeight> CODEC = Codec.either(
			VerticalAnchor.CODEC,
			RecordCodecBuilder.create(
				instance -> instance.group(VerticalAnchor.CODEC.fieldOf("value").forGetter(constantHeight -> constantHeight.value)).apply(instance, ConstantHeight::new)
			)
		)
		.xmap(either -> either.map(ConstantHeight::of, constantHeight -> constantHeight), constantHeight -> Either.left(constantHeight.value));
	private final VerticalAnchor value;

	public static ConstantHeight of(VerticalAnchor verticalAnchor) {
		return new ConstantHeight(verticalAnchor);
	}

	private ConstantHeight(VerticalAnchor verticalAnchor) {
		this.value = verticalAnchor;
	}

	public VerticalAnchor getValue() {
		return this.value;
	}

	@Override
	public int sample(Random random, WorldGenerationContext worldGenerationContext) {
		return this.value.resolveY(worldGenerationContext);
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.CONSTANT;
	}

	public String toString() {
		return this.value.toString();
	}
}
