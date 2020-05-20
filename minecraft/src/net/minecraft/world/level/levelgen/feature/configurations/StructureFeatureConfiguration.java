package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class StructureFeatureConfiguration {
	public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("spacing").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.spacing),
					Codec.INT.fieldOf("separation").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.separation),
					Codec.INT.fieldOf("salt").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.salt)
				)
				.apply(instance, StructureFeatureConfiguration::new)
	);
	private final int spacing;
	private final int separation;
	private final int salt;

	public StructureFeatureConfiguration(int i, int j, int k) {
		this.spacing = i;
		this.separation = j;
		this.salt = k;
	}

	public int spacing() {
		return this.spacing;
	}

	public int separation() {
		return this.separation;
	}

	public int salt() {
		return this.salt;
	}
}
