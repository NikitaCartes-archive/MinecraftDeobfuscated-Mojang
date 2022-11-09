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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration
implements FeatureConfiguration {
    public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.replaceable), ((MapCodec)BlockStateProvider.CODEC.fieldOf("ground_state")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.groundState), ((MapCodec)PlacedFeature.CODEC.fieldOf("vegetation_feature")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationFeature), ((MapCodec)CaveSurface.CODEC.fieldOf("surface")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.surface), ((MapCodec)IntProvider.codec(1, 128).fieldOf("depth")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.depth), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_bottom_block_chance")).forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.extraBottomBlockChance)), ((MapCodec)Codec.intRange(1, 256).fieldOf("vertical_range")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.verticalRange), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("vegetation_chance")).forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.vegetationChance)), ((MapCodec)IntProvider.CODEC.fieldOf("xz_radius")).forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.xzRadius), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("extra_edge_column_chance")).forGetter(vegetationPatchConfiguration -> Float.valueOf(vegetationPatchConfiguration.extraEdgeColumnChance))).apply((Applicative<VegetationPatchConfiguration, ?>)instance, VegetationPatchConfiguration::new));
    public final TagKey<Block> replaceable;
    public final BlockStateProvider groundState;
    public final Holder<PlacedFeature> vegetationFeature;
    public final CaveSurface surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider xzRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchConfiguration(TagKey<Block> tagKey, BlockStateProvider blockStateProvider, Holder<PlacedFeature> holder, CaveSurface caveSurface, IntProvider intProvider, float f, int i, float g, IntProvider intProvider2, float h) {
        this.replaceable = tagKey;
        this.groundState = blockStateProvider;
        this.vegetationFeature = holder;
        this.surface = caveSurface;
        this.depth = intProvider;
        this.extraBottomBlockChance = f;
        this.verticalRange = i;
        this.vegetationChance = g;
        this.xzRadius = intProvider2;
        this.extraEdgeColumnChance = h;
    }
}

