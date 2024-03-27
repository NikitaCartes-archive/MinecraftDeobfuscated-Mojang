package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class UniformHeight extends HeightProvider {
	public static final MapCodec<UniformHeight> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(uniformHeight -> uniformHeight.minInclusive),
					VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(uniformHeight -> uniformHeight.maxInclusive)
				)
				.apply(instance, UniformHeight::new)
	);
	private static final Logger LOGGER = LogUtils.getLogger();
	private final VerticalAnchor minInclusive;
	private final VerticalAnchor maxInclusive;
	private final LongSet warnedFor = new LongOpenHashSet();

	private UniformHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		this.minInclusive = verticalAnchor;
		this.maxInclusive = verticalAnchor2;
	}

	public static UniformHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2) {
		return new UniformHeight(verticalAnchor, verticalAnchor2);
	}

	@Override
	public int sample(RandomSource randomSource, WorldGenerationContext worldGenerationContext) {
		int i = this.minInclusive.resolveY(worldGenerationContext);
		int j = this.maxInclusive.resolveY(worldGenerationContext);
		if (i > j) {
			if (this.warnedFor.add((long)i << 32 | (long)j)) {
				LOGGER.warn("Empty height range: {}", this);
			}

			return i;
		} else {
			return Mth.randomBetweenInclusive(randomSource, i, j);
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
