/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.NoiseSlider;

public record NoiseSettings(int minY, int height, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider topSlideSettings, NoiseSlider bottomSlideSettings, int noiseSizeHorizontal, int noiseSizeVertical, boolean islandNoiseOverride, boolean isAmplified, boolean largeBiomes, TerrainShaper terrainShaper) {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y")).forGetter(NoiseSettings::minY), ((MapCodec)Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height")).forGetter(NoiseSettings::height), ((MapCodec)NoiseSamplingSettings.CODEC.fieldOf("sampling")).forGetter(NoiseSettings::noiseSamplingSettings), ((MapCodec)NoiseSlider.CODEC.fieldOf("top_slide")).forGetter(NoiseSettings::topSlideSettings), ((MapCodec)NoiseSlider.CODEC.fieldOf("bottom_slide")).forGetter(NoiseSettings::bottomSlideSettings), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_horizontal")).forGetter(NoiseSettings::noiseSizeHorizontal), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_vertical")).forGetter(NoiseSettings::noiseSizeVertical), Codec.BOOL.optionalFieldOf("island_noise_override", false, Lifecycle.experimental()).forGetter(NoiseSettings::islandNoiseOverride), Codec.BOOL.optionalFieldOf("amplified", false, Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified), Codec.BOOL.optionalFieldOf("large_biomes", false, Lifecycle.experimental()).forGetter(NoiseSettings::largeBiomes), ((MapCodec)TerrainShaper.CODEC.fieldOf("terrain_shaper")).forGetter(NoiseSettings::terrainShaper)).apply((Applicative<NoiseSettings, ?>)instance, NoiseSettings::new)).comapFlatMap(NoiseSettings::guardY, Function.identity());

    private static DataResult<NoiseSettings> guardY(NoiseSettings noiseSettings) {
        if (noiseSettings.minY() + noiseSettings.height() > DimensionType.MAX_Y + 1) {
            return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
        }
        if (noiseSettings.height() % 16 != 0) {
            return DataResult.error("height has to be a multiple of 16");
        }
        if (noiseSettings.minY() % 16 != 0) {
            return DataResult.error("min_y has to be a multiple of 16");
        }
        return DataResult.success(noiseSettings);
    }

    public static NoiseSettings create(int i, int j, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider noiseSlider, NoiseSlider noiseSlider2, int k, int l, boolean bl, boolean bl2, boolean bl3, TerrainShaper terrainShaper) {
        NoiseSettings noiseSettings = new NoiseSettings(i, j, noiseSamplingSettings, noiseSlider, noiseSlider2, k, l, bl, bl2, bl3, terrainShaper);
        NoiseSettings.guardY(noiseSettings).error().ifPresent(partialResult -> {
            throw new IllegalStateException(partialResult.message());
        });
        return noiseSettings;
    }

    @Deprecated
    public boolean islandNoiseOverride() {
        return this.islandNoiseOverride;
    }

    @Deprecated
    public boolean isAmplified() {
        return this.isAmplified;
    }

    @Deprecated
    public boolean largeBiomes() {
        return this.largeBiomes;
    }
}

