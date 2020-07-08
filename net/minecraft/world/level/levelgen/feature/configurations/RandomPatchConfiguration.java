/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class RandomPatchConfiguration
implements FeatureConfiguration {
    public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("state_provider")).forGetter(randomPatchConfiguration -> randomPatchConfiguration.stateProvider), ((MapCodec)BlockPlacer.CODEC.fieldOf("block_placer")).forGetter(randomPatchConfiguration -> randomPatchConfiguration.blockPlacer), ((MapCodec)BlockState.CODEC.listOf().fieldOf("whitelist")).forGetter(randomPatchConfiguration -> randomPatchConfiguration.whitelist.stream().map(Block::defaultBlockState).collect(Collectors.toList())), ((MapCodec)BlockState.CODEC.listOf().fieldOf("blacklist")).forGetter(randomPatchConfiguration -> ImmutableList.copyOf(randomPatchConfiguration.blacklist)), ((MapCodec)Codec.INT.fieldOf("tries")).orElse(128).forGetter(randomPatchConfiguration -> randomPatchConfiguration.tries), ((MapCodec)Codec.INT.fieldOf("xspread")).orElse(7).forGetter(randomPatchConfiguration -> randomPatchConfiguration.xspread), ((MapCodec)Codec.INT.fieldOf("yspread")).orElse(3).forGetter(randomPatchConfiguration -> randomPatchConfiguration.yspread), ((MapCodec)Codec.INT.fieldOf("zspread")).orElse(7).forGetter(randomPatchConfiguration -> randomPatchConfiguration.zspread), ((MapCodec)Codec.BOOL.fieldOf("can_replace")).orElse(false).forGetter(randomPatchConfiguration -> randomPatchConfiguration.canReplace), ((MapCodec)Codec.BOOL.fieldOf("project")).orElse(true).forGetter(randomPatchConfiguration -> randomPatchConfiguration.project), ((MapCodec)Codec.BOOL.fieldOf("need_water")).orElse(false).forGetter(randomPatchConfiguration -> randomPatchConfiguration.needWater)).apply((Applicative<RandomPatchConfiguration, ?>)instance, RandomPatchConfiguration::new));
    public final BlockStateProvider stateProvider;
    public final BlockPlacer blockPlacer;
    public final Set<Block> whitelist;
    public final Set<BlockState> blacklist;
    public final int tries;
    public final int xspread;
    public final int yspread;
    public final int zspread;
    public final boolean canReplace;
    public final boolean project;
    public final boolean needWater;

    private RandomPatchConfiguration(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer, List<BlockState> list, List<BlockState> list2, int i, int j, int k, int l, boolean bl, boolean bl2, boolean bl3) {
        this(blockStateProvider, blockPlacer, list.stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet()), ImmutableSet.copyOf(list2), i, j, k, l, bl, bl2, bl3);
    }

    private RandomPatchConfiguration(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer, Set<Block> set, Set<BlockState> set2, int i, int j, int k, int l, boolean bl, boolean bl2, boolean bl3) {
        this.stateProvider = blockStateProvider;
        this.blockPlacer = blockPlacer;
        this.whitelist = set;
        this.blacklist = set2;
        this.tries = i;
        this.xspread = j;
        this.yspread = k;
        this.zspread = l;
        this.canReplace = bl;
        this.project = bl2;
        this.needWater = bl3;
    }

    public static class GrassConfigurationBuilder {
        private final BlockStateProvider stateProvider;
        private final BlockPlacer blockPlacer;
        private Set<Block> whitelist = ImmutableSet.of();
        private Set<BlockState> blacklist = ImmutableSet.of();
        private int tries = 64;
        private int xspread = 7;
        private int yspread = 3;
        private int zspread = 7;
        private boolean canReplace;
        private boolean project = true;
        private boolean needWater = false;

        public GrassConfigurationBuilder(BlockStateProvider blockStateProvider, BlockPlacer blockPlacer) {
            this.stateProvider = blockStateProvider;
            this.blockPlacer = blockPlacer;
        }

        public GrassConfigurationBuilder whitelist(Set<Block> set) {
            this.whitelist = set;
            return this;
        }

        public GrassConfigurationBuilder blacklist(Set<BlockState> set) {
            this.blacklist = set;
            return this;
        }

        public GrassConfigurationBuilder tries(int i) {
            this.tries = i;
            return this;
        }

        public GrassConfigurationBuilder xspread(int i) {
            this.xspread = i;
            return this;
        }

        public GrassConfigurationBuilder yspread(int i) {
            this.yspread = i;
            return this;
        }

        public GrassConfigurationBuilder zspread(int i) {
            this.zspread = i;
            return this;
        }

        public GrassConfigurationBuilder canReplace() {
            this.canReplace = true;
            return this;
        }

        public GrassConfigurationBuilder noProjection() {
            this.project = false;
            return this;
        }

        public GrassConfigurationBuilder needWater() {
            this.needWater = true;
            return this;
        }

        public RandomPatchConfiguration build() {
            return new RandomPatchConfiguration(this.stateProvider, this.blockPlacer, this.whitelist, this.blacklist, this.tries, this.xspread, this.yspread, this.zspread, this.canReplace, this.project, this.needWater);
        }
    }
}

