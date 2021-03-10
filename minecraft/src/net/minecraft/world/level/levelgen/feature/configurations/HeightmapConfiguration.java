package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapConfiguration implements DecoratorConfiguration {
	public static final Codec<HeightmapConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(heightmapConfiguration -> heightmapConfiguration.heightmap))
				.apply(instance, HeightmapConfiguration::new)
	);
	public final Heightmap.Types heightmap;

	public HeightmapConfiguration(Heightmap.Types types) {
		this.heightmap = types;
	}
}
