/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GeodeConfiguration
implements FeatureConfiguration {
    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
    public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)GeodeBlockSettings.CODEC.fieldOf("blocks")).forGetter(geodeConfiguration -> geodeConfiguration.geodeBlockSettings), ((MapCodec)GeodeLayerSettings.CODEC.fieldOf("layers")).forGetter(geodeConfiguration -> geodeConfiguration.geodeLayerSettings), ((MapCodec)GeodeCrackSettings.CODEC.fieldOf("crack")).forGetter(geodeConfiguration -> geodeConfiguration.geodeCrackSettings), ((MapCodec)CHANCE_RANGE.fieldOf("use_potential_placements_chance")).orElse(0.35).forGetter(geodeConfiguration -> geodeConfiguration.usePotentialPlacementsChance), ((MapCodec)CHANCE_RANGE.fieldOf("use_alternate_layer0_chance")).orElse(0.0).forGetter(geodeConfiguration -> geodeConfiguration.useAlternateLayer0Chance), ((MapCodec)Codec.BOOL.fieldOf("placements_require_layer0_alternate")).orElse(true).forGetter(geodeConfiguration -> geodeConfiguration.placementsRequireLayer0Alternate), ((MapCodec)IntProvider.codec(1, 20).fieldOf("outer_wall_distance")).orElse(UniformInt.of(4, 5)).forGetter(geodeConfiguration -> geodeConfiguration.outerWallDistance), ((MapCodec)IntProvider.codec(1, 20).fieldOf("distribution_points")).orElse(UniformInt.of(3, 4)).forGetter(geodeConfiguration -> geodeConfiguration.distributionPoints), ((MapCodec)IntProvider.codec(0, 10).fieldOf("point_offset")).orElse(UniformInt.of(1, 2)).forGetter(geodeConfiguration -> geodeConfiguration.pointOffset), ((MapCodec)Codec.INT.fieldOf("min_gen_offset")).orElse(-16).forGetter(geodeConfiguration -> geodeConfiguration.minGenOffset), ((MapCodec)Codec.INT.fieldOf("max_gen_offset")).orElse(16).forGetter(geodeConfiguration -> geodeConfiguration.maxGenOffset), ((MapCodec)CHANCE_RANGE.fieldOf("noise_multiplier")).orElse(0.05).forGetter(geodeConfiguration -> geodeConfiguration.noiseMultiplier), ((MapCodec)Codec.INT.fieldOf("invalid_blocks_threshold")).forGetter(geodeConfiguration -> geodeConfiguration.invalidBlocksThreshold)).apply((Applicative<GeodeConfiguration, ?>)instance, GeodeConfiguration::new));
    public final GeodeBlockSettings geodeBlockSettings;
    public final GeodeLayerSettings geodeLayerSettings;
    public final GeodeCrackSettings geodeCrackSettings;
    public final double usePotentialPlacementsChance;
    public final double useAlternateLayer0Chance;
    public final boolean placementsRequireLayer0Alternate;
    public final IntProvider outerWallDistance;
    public final IntProvider distributionPoints;
    public final IntProvider pointOffset;
    public final int minGenOffset;
    public final int maxGenOffset;
    public final double noiseMultiplier;
    public final int invalidBlocksThreshold;

    public GeodeConfiguration(GeodeBlockSettings geodeBlockSettings, GeodeLayerSettings geodeLayerSettings, GeodeCrackSettings geodeCrackSettings, double d, double e, boolean bl, IntProvider intProvider, IntProvider intProvider2, IntProvider intProvider3, int i, int j, double f, int k) {
        this.geodeBlockSettings = geodeBlockSettings;
        this.geodeLayerSettings = geodeLayerSettings;
        this.geodeCrackSettings = geodeCrackSettings;
        this.usePotentialPlacementsChance = d;
        this.useAlternateLayer0Chance = e;
        this.placementsRequireLayer0Alternate = bl;
        this.outerWallDistance = intProvider;
        this.distributionPoints = intProvider2;
        this.pointOffset = intProvider3;
        this.minGenOffset = i;
        this.maxGenOffset = j;
        this.noiseMultiplier = f;
        this.invalidBlocksThreshold = k;
    }
}

