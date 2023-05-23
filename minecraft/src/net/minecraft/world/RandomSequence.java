package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
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
		this(createSequence(l, resourceLocation));
	}

	private static XoroshiroRandomSource createSequence(long l, ResourceLocation resourceLocation) {
		return new XoroshiroRandomSource(RandomSupport.upgradeSeedTo128bitUnmixed(l).xor(seedForKey(resourceLocation)).mixed());
	}

	public static RandomSupport.Seed128bit seedForKey(ResourceLocation resourceLocation) {
		return RandomSupport.seedFromHashOf(resourceLocation.toString());
	}

	public RandomSource random() {
		return this.source;
	}
}
