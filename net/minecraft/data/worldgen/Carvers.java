/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class Carvers {
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> CAVE = Carvers.register("cave", WorldCarver.CAVE.configured(new CaveCarverConfiguration(0.14285715f, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), ConstantFloat.of(0.5f), VerticalAnchor.aboveBottom(11), CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), ConstantFloat.of(-0.7f))));
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> PROTOTYPE_CAVE = Carvers.register("prototype_cave", WorldCarver.CAVE.configured(new CaveCarverConfiguration(0.33333334f, UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(126)), UniformFloat.of(0.1f, 0.9f), VerticalAnchor.aboveBottom(9), CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), UniformFloat.of(0.3f, 1.8f), UniformFloat.of(0.5f, 1.8f), UniformFloat.of(-1.0f, 0.0f))));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> CANYON = Carvers.register("canyon", WorldCarver.CANYON.configured(new CanyonCarverConfiguration(0.02f, BiasedToBottomHeight.of(VerticalAnchor.absolute(20), VerticalAnchor.absolute(67), 8), ConstantFloat.of(3.0f), VerticalAnchor.aboveBottom(11), CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), UniformFloat.of(-0.125f, 0.125f), new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.75f, 1.0f), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), 3, UniformFloat.of(0.75f, 1.0f), 1.0f, 0.0f))));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> PROTOTYPE_CANYON = Carvers.register("prototype_canyon", WorldCarver.CANYON.configured(new CanyonCarverConfiguration(0.02f, UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(67)), ConstantFloat.of(3.0f), VerticalAnchor.aboveBottom(9), CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), UniformFloat.of(-0.125f, 0.125f), new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.75f, 1.0f), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), 3, UniformFloat.of(0.75f, 1.0f), 1.0f, 0.0f))));
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> OCEAN_CAVE = Carvers.register("ocean_cave", WorldCarver.CAVE.configured(new CaveCarverConfiguration(0.06666667f, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), ConstantFloat.of(0.5f), VerticalAnchor.aboveBottom(11), CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), ConstantFloat.of(-0.7f))));
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> PROTOTYPE_OCEAN_CAVE = Carvers.register("prototype_ocean_cave", WorldCarver.CAVE.configured(new CaveCarverConfiguration(0.14285715f, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), UniformFloat.of(0.1f, 0.9f), VerticalAnchor.aboveBottom(9), CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), UniformFloat.of(-1.0f, 0.0f))));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> UNDERWATER_CANYON = Carvers.register("underwater_canyon", WorldCarver.UNDERWATER_CANYON.configured(new CanyonCarverConfiguration(0.02f, BiasedToBottomHeight.of(VerticalAnchor.absolute(20), VerticalAnchor.absolute(67), 8), ConstantFloat.of(3.0f), VerticalAnchor.aboveBottom(11), CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), UniformFloat.of(-0.125f, 0.125f), new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.75f, 1.0f), TrapezoidFloat.of(0.0f, 6.0f, 2.0f), 3, UniformFloat.of(0.75f, 1.0f), 1.0f, 0.0f))));
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> UNDERWATER_CAVE = Carvers.register("underwater_cave", WorldCarver.UNDERWATER_CAVE.configured(new CaveCarverConfiguration(0.06666667f, BiasedToBottomHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.absolute(127), 8), ConstantFloat.of(0.5f), VerticalAnchor.aboveBottom(11), CarverDebugSettings.of(false, Blocks.CRIMSON_BUTTON.defaultBlockState()), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), ConstantFloat.of(-0.7f))));
    public static final ConfiguredWorldCarver<CaveCarverConfiguration> NETHER_CAVE = Carvers.register("nether_cave", WorldCarver.NETHER_CAVE.configured(new CaveCarverConfiguration(0.2f, UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.belowTop(1)), ConstantFloat.of(0.5f), VerticalAnchor.aboveBottom(11), ConstantFloat.of(1.0f), ConstantFloat.of(1.0f), ConstantFloat.of(-0.7f))));
    public static final ConfiguredWorldCarver<CanyonCarverConfiguration> PROTOTYPE_CRACK = Carvers.register("prototype_crack", WorldCarver.CANYON.configured(new CanyonCarverConfiguration(0.00125f, UniformHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)), UniformFloat.of(6.0f, 8.0f), VerticalAnchor.aboveBottom(9), CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()), UniformFloat.of(-0.125f, 0.125f), new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.5f, 1.0f), UniformFloat.of(0.0f, 1.0f), 6, UniformFloat.of(0.25f, 1.0f), 0.0f, 5.0f))));

    private static <WC extends CarverConfiguration> ConfiguredWorldCarver<WC> register(String string, ConfiguredWorldCarver<WC> configuredWorldCarver) {
        return BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_CARVER, string, configuredWorldCarver);
    }
}

