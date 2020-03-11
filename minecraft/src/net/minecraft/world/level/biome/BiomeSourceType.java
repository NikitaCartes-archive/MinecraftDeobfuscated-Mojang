package net.minecraft.world.level.biome;

import java.util.function.Function;
import java.util.function.LongFunction;
import net.minecraft.core.Registry;

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
		"multi_noise", MultiNoiseBiomeSource::new, MultiNoiseBiomeSourceSettings::new
	);
	private final Function<C, T> factory;
	private final LongFunction<C> settingsFactory;

	private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(
		String string, Function<C, T> function, LongFunction<C> longFunction
	) {
		return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType<>(function, longFunction));
	}

	private BiomeSourceType(Function<C, T> function, LongFunction<C> longFunction) {
		this.factory = function;
		this.settingsFactory = longFunction;
	}

	public T create(C biomeSourceSettings) {
		return (T)this.factory.apply(biomeSourceSettings);
	}

	public C createSettings(long l) {
		return (C)this.settingsFactory.apply(l);
	}
}
