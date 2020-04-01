package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
	public final int noiseToCountRatio;
	public final double noiseFactor;
	public final double noiseOffset;
	public final Heightmap.Types heightmap;

	public NoiseCountFactorDecoratorConfiguration(int i, double d, double e, Heightmap.Types types) {
		this.noiseToCountRatio = i;
		this.noiseFactor = d;
		this.noiseOffset = e;
		this.heightmap = types;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("noise_to_count_ratio"),
					dynamicOps.createInt(this.noiseToCountRatio),
					dynamicOps.createString("noise_factor"),
					dynamicOps.createDouble(this.noiseFactor),
					dynamicOps.createString("noise_offset"),
					dynamicOps.createDouble(this.noiseOffset),
					dynamicOps.createString("heightmap"),
					dynamicOps.createString(this.heightmap.getSerializationKey())
				)
			)
		);
	}

	public static NoiseCountFactorDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		int i = dynamic.get("noise_to_count_ratio").asInt(10);
		double d = dynamic.get("noise_factor").asDouble(80.0);
		double e = dynamic.get("noise_offset").asDouble(0.0);
		Heightmap.Types types = Heightmap.Types.getFromKey(dynamic.get("heightmap").asString("OCEAN_FLOOR_WG"));
		return new NoiseCountFactorDecoratorConfiguration(i, d, e, types);
	}

	public static NoiseCountFactorDecoratorConfiguration random(Random random) {
		return new NoiseCountFactorDecoratorConfiguration(
			random.nextInt(80),
			(double)random.nextInt(80),
			(double)(random.nextFloat() / 4.0F),
			random.nextBoolean() ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG
		);
	}
}
