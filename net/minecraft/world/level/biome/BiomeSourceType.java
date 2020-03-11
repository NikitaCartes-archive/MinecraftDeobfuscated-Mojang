/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import java.util.function.Function;
import java.util.function.LongFunction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceSettings;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSourceSettings;

public class BiomeSourceType<C extends BiomeSourceSettings, T extends BiomeSource> {
    public static final BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardColumnBiomeSource> CHECKERBOARD = BiomeSourceType.register("checkerboard", CheckerboardColumnBiomeSource::new, CheckerboardBiomeSourceSettings::new);
    public static final BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> FIXED = BiomeSourceType.register("fixed", FixedBiomeSource::new, FixedBiomeSourceSettings::new);
    public static final BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> VANILLA_LAYERED = BiomeSourceType.register("vanilla_layered", OverworldBiomeSource::new, OverworldBiomeSourceSettings::new);
    public static final BiomeSourceType<TheEndBiomeSourceSettings, TheEndBiomeSource> THE_END = BiomeSourceType.register("the_end", TheEndBiomeSource::new, TheEndBiomeSourceSettings::new);
    public static final BiomeSourceType<MultiNoiseBiomeSourceSettings, MultiNoiseBiomeSource> MULTI_NOISE = BiomeSourceType.register("multi_noise", MultiNoiseBiomeSource::new, MultiNoiseBiomeSourceSettings::new);
    private final Function<C, T> factory;
    private final LongFunction<C> settingsFactory;

    private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(String string, Function<C, T> function, LongFunction<C> longFunction) {
        return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType<C, T>(function, longFunction));
    }

    private BiomeSourceType(Function<C, T> function, LongFunction<C> longFunction) {
        this.factory = function;
        this.settingsFactory = longFunction;
    }

    public T create(C biomeSourceSettings) {
        return (T)((BiomeSource)this.factory.apply(biomeSourceSettings));
    }

    public C createSettings(long l) {
        return (C)((BiomeSourceSettings)this.settingsFactory.apply(l));
    }
}

