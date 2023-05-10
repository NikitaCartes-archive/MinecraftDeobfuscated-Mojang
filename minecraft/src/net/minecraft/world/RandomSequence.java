package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {
	public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(XoroshiroRandomSource.CODEC.fieldOf("source").forGetter(randomSequence -> randomSequence.source))
				.apply(instance, RandomSequence::new)
	);
	private final XoroshiroRandomSource source;

	public RandomSequence(XoroshiroRandomSource xoroshiroRandomSource) {
		this.source = xoroshiroRandomSource;
	}

	public RandomSequence(long l, ResourceLocation resourceLocation) {
		this(new XoroshiroRandomSource(l, (long)resourceLocation.hashCode()));
	}

	public RandomSource random() {
		return this.source;
	}
}
