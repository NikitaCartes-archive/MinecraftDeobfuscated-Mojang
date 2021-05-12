package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrapezoidHeight extends HeightProvider {
	public static final Codec<TrapezoidHeight> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(trapezoidHeight -> trapezoidHeight.minInclusive),
					VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(trapezoidHeight -> trapezoidHeight.maxInclusive),
					Codec.INT.optionalFieldOf("plateau", Integer.valueOf(0)).forGetter(trapezoidHeight -> trapezoidHeight.plateau)
				)
				.apply(instance, TrapezoidHeight::new)
	);
	private static final Logger LOGGER = LogManager.getLogger();
	private final VerticalAnchor minInclusive;
	private final VerticalAnchor maxInclusive;
	private final int plateau;

	private TrapezoidHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		this.minInclusive = verticalAnchor;
		this.maxInclusive = verticalAnchor2;
		this.plateau = i;
	}

	public static TrapezoidHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		return new TrapezoidHeight(verticalAnchor, verticalAnchor2, i);
	}

	public static TrapezoidHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return of(verticalAnchor, verticalAnchor2, 0);
	}

	@Override
	public int sample(Random random, WorldGenerationContext worldGenerationContext) {
		int i = this.minInclusive.resolveY(worldGenerationContext);
		int j = this.maxInclusive.resolveY(worldGenerationContext);
		if (i > j) {
			LOGGER.warn("Empty height range: {}", this);
			return i;
		} else {
			int k = j - i;
			if (this.plateau >= k) {
				return Mth.randomBetweenInclusive(random, i, j);
			} else {
				int l = (k - this.plateau) / 2;
				int m = k - l;
				return i + Mth.randomBetweenInclusive(random, 0, m) + Mth.randomBetweenInclusive(random, 0, l);
			}
		}
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.TRAPEZOID;
	}

	public String toString() {
		return this.plateau == 0
			? "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")"
			: "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
