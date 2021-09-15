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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.NoiseSlider;

public class NoiseSettings {
    public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y")).forGetter(NoiseSettings::minY), ((MapCodec)Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height")).forGetter(NoiseSettings::height), ((MapCodec)NoiseSamplingSettings.CODEC.fieldOf("sampling")).forGetter(NoiseSettings::noiseSamplingSettings), ((MapCodec)NoiseSlider.CODEC.fieldOf("top_slide")).forGetter(NoiseSettings::topSlideSettings), ((MapCodec)NoiseSlider.CODEC.fieldOf("bottom_slide")).forGetter(NoiseSettings::bottomSlideSettings), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_horizontal")).forGetter(NoiseSettings::noiseSizeHorizontal), ((MapCodec)Codec.intRange(1, 4).fieldOf("size_vertical")).forGetter(NoiseSettings::noiseSizeVertical), ((MapCodec)Codec.DOUBLE.fieldOf("density_factor")).forGetter(NoiseSettings::densityFactor), ((MapCodec)Codec.DOUBLE.fieldOf("density_offset")).forGetter(NoiseSettings::densityOffset), ((MapCodec)Codec.BOOL.fieldOf("simplex_surface_noise")).forGetter(NoiseSettings::useSimplexSurfaceNoise), Codec.BOOL.optionalFieldOf("random_density_offset", false, Lifecycle.experimental()).forGetter(NoiseSettings::randomDensityOffset), Codec.BOOL.optionalFieldOf("island_noise_override", false, Lifecycle.experimental()).forGetter(NoiseSettings::islandNoiseOverride), Codec.BOOL.optionalFieldOf("amplified", false, Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified), Codec.BOOL.optionalFieldOf("use_legacy_random", false, Lifecycle.experimental()).forGetter(NoiseSettings::useLegacyRandom)).apply((Applicative<NoiseSettings, ?>)instance, NoiseSettings::new)).comapFlatMap(NoiseSettings::guardY, Function.identity());
    private final int minY;
    private final int height;
    private final NoiseSamplingSettings noiseSamplingSettings;
    private final NoiseSlider topSlideSettings;
    private final NoiseSlider bottomSlideSettings;
    private final int noiseSizeHorizontal;
    private final int noiseSizeVertical;
    private final double densityFactor;
    private final double densityOffset;
    private final boolean useSimplexSurfaceNoise;
    private final boolean randomDensityOffset;
    private final boolean islandNoiseOverride;
    private final boolean isAmplified;
    private final boolean useLegacyRandom;

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

    private NoiseSettings(int i, int j, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider noiseSlider, NoiseSlider noiseSlider2, int k, int l, double d, double e, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        this.minY = i;
        this.height = j;
        this.noiseSamplingSettings = noiseSamplingSettings;
        this.topSlideSettings = noiseSlider;
        this.bottomSlideSettings = noiseSlider2;
        this.noiseSizeHorizontal = k;
        this.noiseSizeVertical = l;
        this.densityFactor = d;
        this.densityOffset = e;
        this.useSimplexSurfaceNoise = bl;
        this.randomDensityOffset = bl2;
        this.islandNoiseOverride = bl3;
        this.isAmplified = bl4;
        this.useLegacyRandom = bl5;
    }

    public static NoiseSettings create(int i, int j, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider noiseSlider, NoiseSlider noiseSlider2, int k, int l, double d, double e, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        NoiseSettings noiseSettings = new NoiseSettings(i, j, noiseSamplingSettings, noiseSlider, noiseSlider2, k, l, d, e, bl, bl2, bl3, bl4, bl5);
        NoiseSettings.guardY(noiseSettings).error().ifPresent(partialResult -> {
            throw new IllegalStateException(partialResult.message());
        });
        return noiseSettings;
    }

    public int minY() {
        return this.minY;
    }

    public int height() {
        return this.height;
    }

    public NoiseSamplingSettings noiseSamplingSettings() {
        return this.noiseSamplingSettings;
    }

    public NoiseSlider topSlideSettings() {
        return this.topSlideSettings;
    }

    public NoiseSlider bottomSlideSettings() {
        return this.bottomSlideSettings;
    }

    public int noiseSizeHorizontal() {
        return this.noiseSizeHorizontal;
    }

    public int noiseSizeVertical() {
        return this.noiseSizeVertical;
    }

    public double densityFactor() {
        return this.densityFactor;
    }

    public double densityOffset() {
        return this.densityOffset;
    }

    @Deprecated
    public boolean useSimplexSurfaceNoise() {
        return this.useSimplexSurfaceNoise;
    }

    @Deprecated
    public boolean randomDensityOffset() {
        return this.randomDensityOffset;
    }

    @Deprecated
    public boolean islandNoiseOverride() {
        return this.islandNoiseOverride;
    }

    @Deprecated
    public boolean isAmplified() {
        return this.isAmplified;
    }

    public boolean useLegacyRandom() {
        return this.useLegacyRandom;
    }
}

