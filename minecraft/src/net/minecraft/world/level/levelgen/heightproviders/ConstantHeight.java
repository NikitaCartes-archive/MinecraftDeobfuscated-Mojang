package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
	public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
	public static final MapCodec<ConstantHeight> CODEC = VerticalAnchor.CODEC.fieldOf("value").xmap(ConstantHeight::new, ConstantHeight::getValue);
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
	public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
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
