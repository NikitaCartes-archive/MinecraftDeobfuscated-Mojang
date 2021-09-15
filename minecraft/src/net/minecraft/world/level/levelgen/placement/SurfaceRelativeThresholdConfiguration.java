package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class SurfaceRelativeThresholdConfiguration implements DecoratorConfiguration {
	public static final Codec<SurfaceRelativeThresholdConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(surfaceRelativeThresholdConfiguration -> surfaceRelativeThresholdConfiguration.heightmap),
					Codec.INT
						.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE))
						.forGetter(surfaceRelativeThresholdConfiguration -> surfaceRelativeThresholdConfiguration.minInclusive),
					Codec.INT
						.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE))
						.forGetter(surfaceRelativeThresholdConfiguration -> surfaceRelativeThresholdConfiguration.maxInclusive)
				)
				.apply(instance, SurfaceRelativeThresholdConfiguration::new)
	);
	public final Heightmap.Types heightmap;
	public final int minInclusive;
	public final int maxInclusive;

	public SurfaceRelativeThresholdConfiguration(Heightmap.Types types, int i, int j) {
		this.heightmap = types;
		this.minInclusive = i;
		this.maxInclusive = j;
	}
}
