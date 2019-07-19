package net.minecraft.world.level.biome;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public class BiomeSourceType<C extends BiomeSourceSettings, T extends BiomeSource> {
	public static final BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardBiomeSource> CHECKERBOARD = register(
		"checkerboard", CheckerboardBiomeSource::new, CheckerboardBiomeSourceSettings::new
	);
	public static final BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> FIXED = register("fixed", FixedBiomeSource::new, FixedBiomeSourceSettings::new);
	public static final BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> VANILLA_LAYERED = register(
		"vanilla_layered", OverworldBiomeSource::new, OverworldBiomeSourceSettings::new
	);
	public static final BiomeSourceType<TheEndBiomeSourceSettings, TheEndBiomeSource> THE_END = register(
		"the_end", TheEndBiomeSource::new, TheEndBiomeSourceSettings::new
	);
	private final Function<C, T> factory;
	private final Supplier<C> settingsFactory;

	private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(
		String string, Function<C, T> function, Supplier<C> supplier
	) {
		return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType<>(function, supplier));
	}

	public BiomeSourceType(Function<C, T> function, Supplier<C> supplier) {
		this.factory = function;
		this.settingsFactory = supplier;
	}

	public T create(C biomeSourceSettings) {
		return (T)this.factory.apply(biomeSourceSettings);
	}

	public C createSettings() {
		return (C)this.settingsFactory.get();
	}
}
