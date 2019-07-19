/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSourceSettings;
import net.minecraft.world.level.biome.CheckerboardBiomeSource;
import net.minecraft.world.level.biome.CheckerboardBiomeSourceSettings;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSourceSettings;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSourceSettings;

public class BiomeSourceType<C extends BiomeSourceSettings, T extends BiomeSource> {
    public static final BiomeSourceType<CheckerboardBiomeSourceSettings, CheckerboardBiomeSource> CHECKERBOARD = BiomeSourceType.register("checkerboard", CheckerboardBiomeSource::new, CheckerboardBiomeSourceSettings::new);
    public static final BiomeSourceType<FixedBiomeSourceSettings, FixedBiomeSource> FIXED = BiomeSourceType.register("fixed", FixedBiomeSource::new, FixedBiomeSourceSettings::new);
    public static final BiomeSourceType<OverworldBiomeSourceSettings, OverworldBiomeSource> VANILLA_LAYERED = BiomeSourceType.register("vanilla_layered", OverworldBiomeSource::new, OverworldBiomeSourceSettings::new);
    public static final BiomeSourceType<TheEndBiomeSourceSettings, TheEndBiomeSource> THE_END = BiomeSourceType.register("the_end", TheEndBiomeSource::new, TheEndBiomeSourceSettings::new);
    private final Function<C, T> factory;
    private final Supplier<C> settingsFactory;

    private static <C extends BiomeSourceSettings, T extends BiomeSource> BiomeSourceType<C, T> register(String string, Function<C, T> function, Supplier<C> supplier) {
        return Registry.register(Registry.BIOME_SOURCE_TYPE, string, new BiomeSourceType<C, T>(function, supplier));
    }

    public BiomeSourceType(Function<C, T> function, Supplier<C> supplier) {
        this.factory = function;
        this.settingsFactory = supplier;
    }

    public T create(C biomeSourceSettings) {
        return (T)((BiomeSource)this.factory.apply(biomeSourceSettings));
    }

    public C createSettings() {
        return (C)((BiomeSourceSettings)this.settingsFactory.get());
    }
}

