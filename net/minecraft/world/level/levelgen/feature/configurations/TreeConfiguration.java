/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration
implements FeatureConfiguration {
    public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("trunk_provider")).forGetter(treeConfiguration -> treeConfiguration.trunkProvider), ((MapCodec)TrunkPlacer.CODEC.fieldOf("trunk_placer")).forGetter(treeConfiguration -> treeConfiguration.trunkPlacer), ((MapCodec)BlockStateProvider.CODEC.fieldOf("foliage_provider")).forGetter(treeConfiguration -> treeConfiguration.foliageProvider), ((MapCodec)FoliagePlacer.CODEC.fieldOf("foliage_placer")).forGetter(treeConfiguration -> treeConfiguration.foliagePlacer), ((MapCodec)BlockStateProvider.CODEC.fieldOf("dirt_provider")).forGetter(treeConfiguration -> treeConfiguration.dirtProvider), ((MapCodec)FeatureSize.CODEC.fieldOf("minimum_size")).forGetter(treeConfiguration -> treeConfiguration.minimumSize), ((MapCodec)TreeDecorator.CODEC.listOf().fieldOf("decorators")).forGetter(treeConfiguration -> treeConfiguration.decorators), ((MapCodec)Codec.BOOL.fieldOf("ignore_vines")).orElse(false).forGetter(treeConfiguration -> treeConfiguration.ignoreVines), ((MapCodec)Codec.BOOL.fieldOf("force_dirt")).orElse(false).forGetter(treeConfiguration -> treeConfiguration.forceDirt)).apply((Applicative<TreeConfiguration, ?>)instance, TreeConfiguration::new));
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider dirtProvider;
    public final TrunkPlacer trunkPlacer;
    public final BlockStateProvider foliageProvider;
    public final FoliagePlacer foliagePlacer;
    public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators;
    public final boolean ignoreVines;
    public final boolean forceDirt;

    protected TreeConfiguration(BlockStateProvider blockStateProvider, TrunkPlacer trunkPlacer, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, BlockStateProvider blockStateProvider3, FeatureSize featureSize, List<TreeDecorator> list, boolean bl, boolean bl2) {
        this.trunkProvider = blockStateProvider;
        this.trunkPlacer = trunkPlacer;
        this.foliageProvider = blockStateProvider2;
        this.foliagePlacer = foliagePlacer;
        this.dirtProvider = blockStateProvider3;
        this.minimumSize = featureSize;
        this.decorators = list;
        this.ignoreVines = bl;
        this.forceDirt = bl2;
    }

    public static class TreeConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        private final TrunkPlacer trunkPlacer;
        public final BlockStateProvider foliageProvider;
        private final FoliagePlacer foliagePlacer;
        private BlockStateProvider dirtProvider;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private boolean ignoreVines;
        private boolean forceDirt;

        public TreeConfigurationBuilder(BlockStateProvider blockStateProvider, TrunkPlacer trunkPlacer, BlockStateProvider blockStateProvider2, FoliagePlacer foliagePlacer, FeatureSize featureSize) {
            this.trunkProvider = blockStateProvider;
            this.trunkPlacer = trunkPlacer;
            this.foliageProvider = blockStateProvider2;
            this.dirtProvider = BlockStateProvider.simple(Blocks.DIRT);
            this.foliagePlacer = foliagePlacer;
            this.minimumSize = featureSize;
        }

        public TreeConfigurationBuilder dirt(BlockStateProvider blockStateProvider) {
            this.dirtProvider = blockStateProvider;
            return this;
        }

        public TreeConfigurationBuilder decorators(List<TreeDecorator> list) {
            this.decorators = list;
            return this;
        }

        public TreeConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public TreeConfigurationBuilder forceDirt() {
            this.forceDirt = true;
            return this;
        }

        public TreeConfiguration build() {
            return new TreeConfiguration(this.trunkProvider, this.trunkPlacer, this.foliageProvider, this.foliagePlacer, this.dirtProvider, this.minimumSize, this.decorators, this.ignoreVines, this.forceDirt);
        }
    }
}

