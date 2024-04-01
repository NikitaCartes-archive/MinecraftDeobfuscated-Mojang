package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public record NoiseSettings(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
	public static final Codec<NoiseSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("min_y").forGetter(NoiseSettings::minY),
						Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(NoiseSettings::height),
						Codec.intRange(1, 4).fieldOf("size_horizontal").forGetter(NoiseSettings::noiseSizeHorizontal),
						Codec.intRange(1, 4).fieldOf("size_vertical").forGetter(NoiseSettings::noiseSizeVertical)
					)
					.apply(instance, NoiseSettings::new)
		)
		.comapFlatMap(NoiseSettings::guardY, Function.identity());
	protected static final NoiseSettings OVERWORLD_NOISE_SETTINGS = create(-64, 384, 1, 2);
	protected static final NoiseSettings NETHER_NOISE_SETTINGS = create(0, 128, 1, 2);
	protected static final NoiseSettings END_NOISE_SETTINGS = create(0, 128, 2, 1);
	protected static final NoiseSettings CAVES_NOISE_SETTINGS = create(-64, 192, 1, 2);
	protected static final NoiseSettings FLOATING_ISLANDS_NOISE_SETTINGS = create(0, 256, 2, 1);
	protected static final NoiseSettings POTATO_NOISE_SETTINGS = create(0, 256, 4, 1);

	private static DataResult<NoiseSettings> guardY(NoiseSettings noiseSettings) {
		if (noiseSettings.minY() + noiseSettings.height() > DimensionType.MAX_Y + 1) {
			return DataResult.error(() -> "min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
		} else if (noiseSettings.height() % 16 != 0) {
			return DataResult.error(() -> "height has to be a multiple of 16");
		} else {
			return noiseSettings.minY() % 16 != 0 ? DataResult.error(() -> "min_y has to be a multiple of 16") : DataResult.success(noiseSettings);
		}
	}

	public static NoiseSettings create(int i, int j, int k, int l) {
		NoiseSettings noiseSettings = new NoiseSettings(i, j, k, l);
		guardY(noiseSettings).error().ifPresent(partialResult -> {
			throw new IllegalStateException(partialResult.message());
		});
		return noiseSettings;
	}

	public int getCellHeight() {
		return QuartPos.toBlock(this.noiseSizeVertical());
	}

	public int getCellWidth() {
		return QuartPos.toBlock(this.noiseSizeHorizontal());
	}

	public NoiseSettings clampToHeightAccessor(LevelHeightAccessor levelHeightAccessor) {
		int i = Math.max(this.minY, levelHeightAccessor.getMinBuildHeight());
		int j = Math.min(this.minY + this.height, levelHeightAccessor.getMaxBuildHeight()) - i;
		return new NoiseSettings(i, j, this.noiseSizeHorizontal, this.noiseSizeVertical);
	}
}
