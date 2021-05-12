package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UniformHeight extends HeightProvider {
	public static final Codec<UniformHeight> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(uniformHeight -> uniformHeight.minInclusive),
					VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(uniformHeight -> uniformHeight.maxInclusive)
				)
				.apply(instance, UniformHeight::new)
	);
	private static final Logger LOGGER = LogManager.getLogger();
	private final VerticalAnchor minInclusive;
	private final VerticalAnchor maxInclusive;

	private UniformHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		this.minInclusive = verticalAnchor;
		this.maxInclusive = verticalAnchor2;
	}

	public static UniformHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return new UniformHeight(verticalAnchor, verticalAnchor2);
	}

	@Override
	public int sample(Random random, WorldGenerationContext worldGenerationContext) {
		int i = this.minInclusive.resolveY(worldGenerationContext);
		int j = this.maxInclusive.resolveY(worldGenerationContext);
		if (i > j) {
			LOGGER.warn("Empty height range: {}", this);
			return i;
		} else {
			return Mth.randomBetweenInclusive(random, i, j);
		}
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.UNIFORM;
	}

	public String toString() {
		return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
