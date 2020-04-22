/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class TreeConfiguration
implements FeatureConfiguration {
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider leavesProvider;
    public final List<TreeDecorator> decorators;
    public transient boolean fromSapling;
    public final FoliagePlacer foliagePlacer;
    public final TrunkPlacer trunkPlacer;
    public final FeatureSize minimumSize;
    public final int maxWaterDepth;
    public final boolean ignoreVines;
    public final Heightmap.Types heightmap;

    protected TreeConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, TrunkPlacer trunkPlacer, FeatureSize featureSize, List<TreeDecorator> list, int i, boolean bl, Heightmap.Types types) {
        this.trunkProvider = blockStateProvider;
        this.leavesProvider = blockStateProvider2;
        this.decorators = list;
        this.foliagePlacer = foliagePlacer;
        this.minimumSize = featureSize;
        this.trunkPlacer = trunkPlacer;
        this.maxWaterDepth = i;
        this.ignoreVines = bl;
        this.heightmap = types;
    }

    public void setFromSapling() {
        this.fromSapling = true;
    }

    public TreeConfiguration withDecorators(List<TreeDecorator> list) {
        return new TreeConfiguration(this.trunkProvider, this.leavesProvider, this.foliagePlacer, this.trunkPlacer, this.minimumSize, list, this.maxWaterDepth, this.ignoreVines, this.heightmap);
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("trunk_provider"), this.trunkProvider.serialize(dynamicOps)).put(dynamicOps.createString("leaves_provider"), this.leavesProvider.serialize(dynamicOps)).put(dynamicOps.createString("decorators"), dynamicOps.createList(this.decorators.stream().map(treeDecorator -> treeDecorator.serialize(dynamicOps)))).put(dynamicOps.createString("foliage_placer"), this.foliagePlacer.serialize(dynamicOps)).put(dynamicOps.createString("trunk_placer"), this.trunkPlacer.serialize(dynamicOps)).put(dynamicOps.createString("minimum_size"), this.minimumSize.serialize(dynamicOps)).put(dynamicOps.createString("max_water_depth"), dynamicOps.createInt(this.maxWaterDepth)).put(dynamicOps.createString("ignore_vines"), dynamicOps.createBoolean(this.ignoreVines)).put(dynamicOps.createString("heightmap"), dynamicOps.createString(this.heightmap.getSerializationKey()));
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(builder.build()));
    }

    public static <T> TreeConfiguration deserialize(Dynamic<T> dynamic2) {
        BlockStateProviderType<T> blockStateProviderType = Registry.BLOCKSTATE_PROVIDER_TYPES.get(new ResourceLocation(dynamic2.get("trunk_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        BlockStateProviderType<T> blockStateProviderType2 = Registry.BLOCKSTATE_PROVIDER_TYPES.get(new ResourceLocation(dynamic2.get("leaves_provider").get("type").asString().orElseThrow(RuntimeException::new)));
        FoliagePlacerType<T> foliagePlacerType = Registry.FOLIAGE_PLACER_TYPES.get(new ResourceLocation(dynamic2.get("foliage_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        TrunkPlacerType<T> trunkPlacerType = Registry.TRUNK_PLACER_TYPES.get(new ResourceLocation(dynamic2.get("trunk_placer").get("type").asString().orElseThrow(RuntimeException::new)));
        FeatureSizeType<T> featureSizeType = Registry.FEATURE_SIZE_TYPES.get(new ResourceLocation(dynamic2.get("minimum_size").get("type").asString().orElseThrow(RuntimeException::new)));
        return new TreeConfiguration((BlockStateProvider)blockStateProviderType.deserialize(dynamic2.get("trunk_provider").orElseEmptyMap()), (BlockStateProvider)blockStateProviderType2.deserialize(dynamic2.get("leaves_provider").orElseEmptyMap()), (FoliagePlacer)foliagePlacerType.deserialize(dynamic2.get("foliage_placer").orElseEmptyMap()), (TrunkPlacer)trunkPlacerType.deserialize(dynamic2.get("trunk_placer").orElseEmptyMap()), (FeatureSize)featureSizeType.deserialize(dynamic2.get("minimum_size").orElseEmptyMap()), dynamic2.get("decorators").asList(dynamic -> Registry.TREE_DECORATOR_TYPES.get(new ResourceLocation(dynamic.get("type").asString().orElseThrow(RuntimeException::new))).deserialize((Dynamic<?>)dynamic)), dynamic2.get("max_water_depth").asInt(0), dynamic2.get("ignore_vines").asBoolean(false), Heightmap.Types.getFromKey(dynamic2.get("heightmap").asString("")));
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        public final BlockStateProvider leavesProvider;
        private final FoliagePlacer foliagePlacer;
        private final TrunkPlacer trunkPlacer;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private int maxWaterDepth;
        private boolean ignoreVines;
        private Heightmap.Types heightmap = Heightmap.Types.OCEAN_FLOOR;

        public TreeConfigurationBuilder(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, TrunkPlacer trunkPlacer, FeatureSize featureSize) {
            this.trunkProvider = blockStateProvider;
            this.leavesProvider = blockStateProvider2;
            this.foliagePlacer = foliagePlacer;
            this.trunkPlacer = trunkPlacer;
            this.minimumSize = featureSize;
        }

        public TreeConfigurationBuilder decorators(List<TreeDecorator> list) {
            this.decorators = list;
            return this;
        }

        public TreeConfigurationBuilder maxWaterDepth(int i) {
            this.maxWaterDepth = i;
            return this;
        }

        public TreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public TreeConfigurationBuilder heightmap(Heightmap.Types types) {
            this.heightmap = types;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(this.trunkProvider, this.leavesProvider, this.foliagePlacer, this.trunkPlacer, this.minimumSize, this.decorators, this.maxWaterDepth, this.ignoreVines, this.heightmap);
        }
    }
}

