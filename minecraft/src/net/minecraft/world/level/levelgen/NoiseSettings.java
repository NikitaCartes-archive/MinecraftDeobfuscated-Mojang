package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.QuartPos;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(
	int minY,
	int height,
	NoiseSamplingSettings noiseSamplingSettings,
	NoiseSlider topSlideSettings,
	NoiseSlider bottomSlideSettings,
	int noiseSizeHorizontal,
	int noiseSizeVertical,
	TerrainShaper terrainShaper
) {
	public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
						Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
						NoiseSamplingSettings.CODEC.fieldOf("sampling").forGetter(NoiseSettings::noiseSamplingSettings),
						NoiseSlider.CODEC.fieldOf("top_slide").forGetter(NoiseSettings::topSlideSettings),
						NoiseSlider.CODEC.fieldOf("bottom_slide").forGetter(NoiseSettings::bottomSlideSettings),
						Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
						Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical),
						TerrainShaper.CODEC.fieldOf("terrain_shaper").forGetter(NoiseSettings::terrainShaper)
					)
					.apply(instance, NoiseSettings::new)
		)
		.comapFlatMap(NoiseSettings::guardY, Function.identity());
	static final NoiseSettings NETHER_NOISE_SETTINGS = create(
		0, 128, new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0), new NoiseSlider(0.9375, 3, 0), new NoiseSlider(2.5, 4, -1), 1, 2, TerrainProvider.nether()
	);
	static final NoiseSettings END_NOISE_SETTINGS = create(
		0, 128, new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0), new NoiseSlider(-23.4375, 64, -46), new NoiseSlider(-0.234375, 7, 1), 2, 1, TerrainProvider.end()
	);
	static final NoiseSettings CAVES_NOISE_SETTINGS = create(
		-64, 192, new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0), new NoiseSlider(0.9375, 3, 0), new NoiseSlider(2.5, 4, -1), 1, 2, TerrainProvider.caves()
	);
	static final NoiseSettings FLOATING_ISLANDS_NOISE_SETTINGS = create(
		0,
		256,
		new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0),
		new NoiseSlider(-23.4375, 64, -46),
		new NoiseSlider(-0.234375, 7, 1),
		2,
		1,
		TerrainProvider.floatingIslands()
	);

	private static DataResult<NoiseSettings> guardY(NoiseSettings noiseSettings) {
		if (noiseSettings.minY() + noiseSettings.height() > DimensionType.MAX_Y + 1) {
			return DataResult.error("min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
		} else if (noiseSettings.height() % 16 != 0) {
			return DataResult.error("height has to be a multiple of 16");
		} else {
			return noiseSettings.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(noiseSettings);
		}
	}

	public static NoiseSettings create(
		int i, int j, NoiseSamplingSettings noiseSamplingSettings, NoiseSlider noiseSlider, NoiseSlider noiseSlider2, int k, int l, TerrainShaper terrainShaper
	) {
		NoiseSettings noiseSettings = new NoiseSettings(i, j, noiseSamplingSettings, noiseSlider, noiseSlider2, k, l, terrainShaper);
		guardY(noiseSettings).error().ifPresent(partialResult -> {
			throw new IllegalStateException(partialResult.message());
		});
		return noiseSettings;
	}

	static NoiseSettings overworldNoiseSettings(boolean bl) {
		return create(
			-64,
			384,
			new NoiseSamplingSettings(1.0, 1.0, 80.0, 160.0),
			new NoiseSlider(-0.078125, 2, bl ? 0 : 8),
			new NoiseSlider(bl ? 0.4 : 0.1171875, 3, 0),
			1,
			2,
			TerrainProvider.overworld(bl)
		);
	}

	public int getCellHeight() {
		return QuartPos.toBlock(this.noiseSizeVertical());
	}

	public int getCellWidth() {
		return QuartPos.toBlock(this.noiseSizeHorizontal());
	}

	public int getCellCountY() {
		return this.height() / this.getCellHeight();
	}

	public int getMinCellY() {
		return Mth.intFloorDiv(this.minY(), this.getCellHeight());
	}
}
