package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public class StructureFeatureConfiguration {
	public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.intRange(0, 4096).fieldOf("spacing").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.spacing),
						Codec.intRange(0, 4096).fieldOf("separation").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.separation),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(structureFeatureConfiguration -> structureFeatureConfiguration.salt)
					)
					.apply(instance, StructureFeatureConfiguration::new)
		)
		.comapFlatMap(
			structureFeatureConfiguration -> structureFeatureConfiguration.spacing <= structureFeatureConfiguration.separation
					? DataResult.error("Spacing has to be smaller than separation")
					: DataResult.success(structureFeatureConfiguration),
			Function.identity()
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
