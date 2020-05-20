package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class OceanRuinConfiguration implements FeatureConfiguration {
	public static final Codec<OceanRuinConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					OceanRuinFeature.Type.CODEC.fieldOf("biome_temp").forGetter(oceanRuinConfiguration -> oceanRuinConfiguration.biomeTemp),
					Codec.FLOAT.fieldOf("large_probability").forGetter(oceanRuinConfiguration -> oceanRuinConfiguration.largeProbability),
					Codec.FLOAT.fieldOf("cluster_probability").forGetter(oceanRuinConfiguration -> oceanRuinConfiguration.clusterProbability)
				)
				.apply(instance, OceanRuinConfiguration::new)
	);
	public final OceanRuinFeature.Type biomeTemp;
	public final float largeProbability;
	public final float clusterProbability;

	public OceanRuinConfiguration(OceanRuinFeature.Type type, float f, float g) {
		this.biomeTemp = type;
		this.largeProbability = f;
		this.clusterProbability = g;
	}
}
