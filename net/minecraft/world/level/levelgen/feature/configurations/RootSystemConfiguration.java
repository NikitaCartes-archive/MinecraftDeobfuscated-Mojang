/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration
implements FeatureConfiguration {
    public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)PlacedFeature.CODEC.fieldOf("feature")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.treeFeature), ((MapCodec)Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.requiredVerticalSpaceForTree), ((MapCodec)Codec.intRange(1, 64).fieldOf("root_radius")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootRadius), ((MapCodec)TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootReplaceable), ((MapCodec)BlockStateProvider.CODEC.fieldOf("root_state_provider")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootStateProvider), ((MapCodec)Codec.intRange(1, 256).fieldOf("root_placement_attempts")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootPlacementAttempts), ((MapCodec)Codec.intRange(1, 4096).fieldOf("root_column_max_height")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.rootColumnMaxHeight), ((MapCodec)Codec.intRange(1, 64).fieldOf("hanging_root_radius")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootRadius), ((MapCodec)Codec.intRange(0, 16).fieldOf("hanging_roots_vertical_span")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootsVerticalSpan), ((MapCodec)BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootStateProvider), ((MapCodec)Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.hangingRootPlacementAttempts), ((MapCodec)Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.allowedVerticalWaterForTree), ((MapCodec)BlockPredicate.CODEC.fieldOf("allowed_tree_position")).forGetter(rootSystemConfiguration -> rootSystemConfiguration.allowedTreePosition)).apply((Applicative<RootSystemConfiguration, ?>)instance, RootSystemConfiguration::new));
    public final Holder<PlacedFeature> treeFeature;
    public final int requiredVerticalSpaceForTree;
    public final int rootRadius;
    public final TagKey<Block> rootReplaceable;
    public final BlockStateProvider rootStateProvider;
    public final int rootPlacementAttempts;
    public final int rootColumnMaxHeight;
    public final int hangingRootRadius;
    public final int hangingRootsVerticalSpan;
    public final BlockStateProvider hangingRootStateProvider;
    public final int hangingRootPlacementAttempts;
    public final int allowedVerticalWaterForTree;
    public final BlockPredicate allowedTreePosition;

    public RootSystemConfiguration(Holder<PlacedFeature> holder, int i, int j, TagKey<Block> tagKey, BlockStateProvider blockStateProvider, int k, int l, int m, int n, BlockStateProvider blockStateProvider2, int o, int p, BlockPredicate blockPredicate) {
        this.treeFeature = holder;
        this.requiredVerticalSpaceForTree = i;
        this.rootRadius = j;
        this.rootReplaceable = tagKey;
        this.rootStateProvider = blockStateProvider;
        this.rootPlacementAttempts = k;
        this.rootColumnMaxHeight = l;
        this.hangingRootRadius = m;
        this.hangingRootsVerticalSpan = n;
        this.hangingRootStateProvider = blockStateProvider2;
        this.hangingRootPlacementAttempts = o;
        this.allowedVerticalWaterForTree = p;
        this.allowedTreePosition = blockPredicate;
    }
}

