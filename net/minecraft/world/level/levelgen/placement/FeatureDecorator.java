/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.BiasedRangeDecorator;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecorator;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecorator;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.CountDecorator;
import net.minecraft.world.level.levelgen.placement.CountNoiseDecorator;
import net.minecraft.world.level.levelgen.placement.CountWithExtraChanceDecorator;
import net.minecraft.world.level.levelgen.placement.DarkOakTreePlacementDecorator;
import net.minecraft.world.level.levelgen.placement.DecoratedDecorator;
import net.minecraft.world.level.levelgen.placement.DecoratedDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.DepthAverageDecorator;
import net.minecraft.world.level.levelgen.placement.EmeraldPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndGatewayPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.EndIslandPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightMapWorldSurfaceDecorator;
import net.minecraft.world.level.levelgen.placement.HeightmapDecorator;
import net.minecraft.world.level.levelgen.placement.HeightmapDoubleDecorator;
import net.minecraft.world.level.levelgen.placement.IcebergPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeLavaPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.LakeWaterPlacementDecorator;
import net.minecraft.world.level.levelgen.placement.NoiseBasedDecorator;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.NopePlacementDecorator;
import net.minecraft.world.level.levelgen.placement.RangeDecorator;
import net.minecraft.world.level.levelgen.placement.Spread32Decorator;
import net.minecraft.world.level.levelgen.placement.SquareDecorator;
import net.minecraft.world.level.levelgen.placement.TopSolidHeightMapDecorator;
import net.minecraft.world.level.levelgen.placement.VeryBiasedRangeDecorator;
import net.minecraft.world.level.levelgen.placement.nether.CountMultiLayerDecorator;
import net.minecraft.world.level.levelgen.placement.nether.FireDecorator;
import net.minecraft.world.level.levelgen.placement.nether.GlowstoneDecorator;
import net.minecraft.world.level.levelgen.placement.nether.MagmaDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
    public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE = FeatureDecorator.register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE = FeatureDecorator.register("chance", new ChanceDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountConfiguration> COUNT = FeatureDecorator.register("count", new CountDecorator(CountConfiguration.CODEC));
    public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> COUNT_NOISE = FeatureDecorator.register("count_noise", new CountNoiseDecorator(NoiseDependantDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> COUNT_NOISE_BIASED = FeatureDecorator.register("count_noise_biased", new NoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA = FeatureDecorator.register("count_extra", new CountWithExtraChanceDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> SQUARE = FeatureDecorator.register("square", new SquareDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP = FeatureDecorator.register("heightmap", new HeightmapDecorator<NoneDecoratorConfiguration>(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_SPREAD_DOUBLE = FeatureDecorator.register("heightmap_spread_double", new HeightmapDoubleDecorator<NoneDecoratorConfiguration>(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP = FeatureDecorator.register("top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_WORLD_SURFACE = FeatureDecorator.register("heightmap_world_surface", new HeightMapWorldSurfaceDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE = FeatureDecorator.register("range", new RangeDecorator(RangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE_BIASED = FeatureDecorator.register("range_biased", new BiasedRangeDecorator(RangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE_VERY_BIASED = FeatureDecorator.register("range_very_biased", new VeryBiasedRangeDecorator(RangeDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<DepthAverageConfigation> DEPTH_AVERAGE = FeatureDecorator.register("depth_average", new DepthAverageDecorator(DepthAverageConfigation.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> SPREAD_32_ABOVE = FeatureDecorator.register("spread_32_above", new Spread32Decorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK = FeatureDecorator.register("carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountConfiguration> FIRE = FeatureDecorator.register("fire", new FireDecorator(CountConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> MAGMA = FeatureDecorator.register("magma", new MagmaDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> EMERALD_ORE = FeatureDecorator.register("emerald_ore", new EmeraldPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE = FeatureDecorator.register("lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<ChanceDecoratorConfiguration> WATER_LAKE = FeatureDecorator.register("water_lake", new LakeWaterPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountConfiguration> GLOWSTONE = FeatureDecorator.register("glowstone", new GlowstoneDecorator(CountConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY = FeatureDecorator.register("end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE = FeatureDecorator.register("dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> ICEBERG = FeatureDecorator.register("iceberg", new IcebergPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<NoneDecoratorConfiguration> END_ISLAND = FeatureDecorator.register("end_island", new EndIslandPlacementDecorator(NoneDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<DecoratedDecoratorConfiguration> DECORATED = FeatureDecorator.register("decorated", new DecoratedDecorator(DecoratedDecoratorConfiguration.CODEC));
    public static final FeatureDecorator<CountConfiguration> COUNT_MULTILAYER = FeatureDecorator.register("count_multilayer", new CountMultiLayerDecorator(CountConfiguration.CODEC));
    private final Codec<ConfiguredDecorator<DC>> configuredCodec;

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String string, G featureDecorator) {
        return (G)Registry.register(Registry.DECORATOR, string, featureDecorator);
    }

    public FeatureDecorator(Codec<DC> codec) {
        this.configuredCodec = ((MapCodec)codec.fieldOf("config")).xmap(decoratorConfiguration -> new ConfiguredDecorator<DecoratorConfiguration>(this, (DecoratorConfiguration)decoratorConfiguration), ConfiguredDecorator::config).codec();
    }

    public ConfiguredDecorator<DC> configured(DC decoratorConfiguration) {
        return new ConfiguredDecorator<DC>(this, decoratorConfiguration);
    }

    public Codec<ConfiguredDecorator<DC>> configuredCodec() {
        return this.configuredCodec;
    }

    public abstract Stream<BlockPos> getPositions(DecorationContext var1, Random var2, DC var3, BlockPos var4);

    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
    }
}

