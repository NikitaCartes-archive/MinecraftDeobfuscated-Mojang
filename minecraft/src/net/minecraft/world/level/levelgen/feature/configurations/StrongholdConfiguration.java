package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StrongholdConfiguration {
	public static final Codec<StrongholdConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(0, 1023).fieldOf("distance").forGetter(StrongholdConfiguration::distance),
					Codec.intRange(0, 1023).fieldOf("spread").forGetter(StrongholdConfiguration::spread),
					Codec.intRange(1, 4095).fieldOf("count").forGetter(StrongholdConfiguration::count)
				)
				.apply(instance, StrongholdConfiguration::new)
	);
	private final int distance;
	private final int spread;
	private final int count;

	public StrongholdConfiguration(int i, int j, int k) {
		this.distance = i;
		this.spread = j;
		this.count = k;
	}

	public int distance() {
		return this.distance;
	}

	public int spread() {
		return this.spread;
	}

	public int count() {
		return this.count;
	}
}
