package net.minecraft.world.level.biome;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.LevelData;

public class BiomeSourceType<C extends BiomeSourceSettings, T extends BiomeSource> {
	public static final BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardColumnBiomeSource> CHECKERBOARD = register(
		"checkerboard", CheckerboardColumnBiomeSource::new, CheckerboardBiomeSourceSettings::new
	);
	public static final BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> FIXED = register("fixed", FixedBiomeSource::new, FixedBiomeSourceSettings::new);
	public static final BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> VANILLA_LAYERED = register(
		"vanilla_layered", OverworldBiomeSource::new, OverworldBiomeSourceSettings::new
	);
	public static final BiomeSourceType<TheEndBiomeSourceSettings, TheEndBiomeSource> THE_END = register(
		"the_end", TheEndBiomeSource::new, TheEndBiomeSourceSettings::new
	);
	public static final BiomeSourceType<MultiNoiseBiomeSourceSettings, MultiNoiseBiomeSource> MULTI_NOISE = register(
		"multi_noise", MultiNoiseBiomeSource::new, levelData -> new MultiNoiseBiomeSourceSettings(levelData.getSeed())
	);
	private final Function<C, T> factory;
	private final Function<LevelData, C> settingsFactory;

	private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(
		String string, Function<C, T> function, Function<LevelData, C> function2
	) {
		return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType<>(function, function2));
	}

	private BiomeSourceType(Function<C, T> function, Function<LevelData, C> function2) {
		this.factory = function;
		this.settingsFactory = function2;
	}

	public T create(C biomeSourceSettings) {
		return (T)this.factory.apply(biomeSourceSettings);
	}

	public C createSettings(LevelData levelData) {
		return (C)this.settingsFactory.apply(levelData);
	}
}
