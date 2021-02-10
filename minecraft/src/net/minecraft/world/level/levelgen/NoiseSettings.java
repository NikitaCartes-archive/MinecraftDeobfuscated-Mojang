package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;

public class NoiseSettings {
	public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
						Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
						NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings),
						NoiseSlideSettings.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings),
						NoiseSlideSettings.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings),
						Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
						Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical),
						Codec.DOUBLE.fieldOf("density_factor").forGetter(NoiseSettings::densityFactor),
						Codec.DOUBLE.fieldOf("density_offset").forGetter(NoiseSettings::densityOffset),
						Codec.BOOL.fieldOf("simplex_surface_noise").forGetter(NoiseSettings::useSimplexSurfaceNoise),
						Codec.BOOL.optionalFieldOf("random_density_offset", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::randomDensityOffset),
						Codec.BOOL.optionalFieldOf("island_noise_override", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::islandNoiseOverride),
						Codec.BOOL.optionalFieldOf("amplified", Boolean.valueOf(false), Lifecycle.experimental()).forGetter(NoiseSettings::isAmplified)
					)
					.apply(instance, NoiseSettings::new)
		)
		.comapFlatMap(NoiseSettings::guardY, Function.identity());
	private final int minY;
	private final int height;
	private final NoiseSamplingSettings noiseSamplingSettings;
	private final NoiseSlideSettings topSlideSettings;
	private final NoiseSlideSettings bottomSlideSettings;
	private final int noiseSizeHorizontal;
	private final int noiseSizeVertical;
	private final double densityFactor;
	private final double densityOffset;
	private final boolean useSimplexSurfaceNoise;
	private final boolean randomDensityOffset;
	private final boolean islandNoiseOverride;
	private final boolean isAmplified;

	private static DataResult<NoiseSettings> guardY(NoiseSettings noiseSettings) {
		if (noiseSettings.minY() + noiseSettings.height() > DimensionType.MAX_Y + 1) {
			return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
		} else if (noiseSettings.height() % 16 != 0) {
			return DataResult.error("height has to be a multiple of 16");
		} else {
			return noiseSettings.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(noiseSettings);
		}
	}

	private NoiseSettings(
		int i,
		int j,
		NoiseSamplingSettings noiseSamplingSettings,
		NoiseSlideSettings noiseSlideSettings,
		NoiseSlideSettings noiseSlideSettings2,
		int k,
		int l,
		double d,
		double e,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4
	) {
		this.minY = i;
		this.height = j;
		this.noiseSamplingSettings = noiseSamplingSettings;
		this.topSlideSettings = noiseSlideSettings;
		this.bottomSlideSettings = noiseSlideSettings2;
		this.noiseSizeHorizontal = k;
		this.noiseSizeVertical = l;
		this.densityFactor = d;
		this.densityOffset = e;
		this.useSimplexSurfaceNoise = bl;
		this.randomDensityOffset = bl2;
		this.islandNoiseOverride = bl3;
		this.isAmplified = bl4;
	}

	public static NoiseSettings create(
		int i,
		int j,
		NoiseSamplingSettings noiseSamplingSettings,
		NoiseSlideSettings noiseSlideSettings,
		NoiseSlideSettings noiseSlideSettings2,
		int k,
		int l,
		double d,
		double e,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4
	) {
		NoiseSettings noiseSettings = new NoiseSettings(i, j, noiseSamplingSettings, noiseSlideSettings, noiseSlideSettings2, k, l, d, e, bl, bl2, bl3, bl4);
		guardY(noiseSettings).error().ifPresent(partialResult -> {
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

	public NoiseSlideSettings topSlideSettings() {
		return this.topSlideSettings;
	}

	public NoiseSlideSettings bottomSlideSettings() {
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
}
