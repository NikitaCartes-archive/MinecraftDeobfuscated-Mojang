/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.TrapezoidFloat;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

public class Carvers {
    public static final ConfiguredWorldCarver<CarverConfiguration> CAVE = Carvers.register("cave", WorldCarver.CAVE.configured(new CarverConfiguration(0.33333334f, CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()))));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CANYON = Carvers.register("canyon", WorldCarver.CANYON.configured(new CanyonCarverConfiguration(0.02f, CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), VerticalAnchor.absolute(10), VerticalAnchor.absolute(67), UniformInt.fixed(3), UniformFloat.of(0.75f, 0.25f), UniformFloat.of(-0.125f, 0.25f), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), 3, UniformFloat.of(0.75f, 0.25f), 1.0f, 0.0f)));
    public static final ConfiguredWorldCarver<CarverConfiguration> OCEAN_CAVE = Carvers.register("ocean_cave", WorldCarver.CAVE.configured(new CarverConfiguration(0.14285715f, CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()))));
    public static final ConfiguredWorldCarver<CarverConfiguration> NETHER_CAVE = Carvers.register("nether_cave", WorldCarver.NETHER_CAVE.configured(new CarverConfiguration(0.2f)));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CRACK = Carvers.register("crack", WorldCarver.CANYON.configured(new CanyonCarverConfiguration(0.00125f, CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()), VerticalAnchor.absolute(40), VerticalAnchor.absolute(80), UniformInt.of(6, 2), UniformFloat.of(0.5f, 0.5f), UniformFloat.of(-0.125f, 0.25f), UniformFloat.of(0.0f, 1.0f), 6, UniformFloat.of(0.25f, 0.75f), 0.0f, 5.0f)));

    private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
    }
}

