/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
    public final BlockStateProvider fillingProvider;
    public final BlockStateProvider innerLayerProvider;
    public final BlockStateProvider alternateInnerLayerProvider;
    public final BlockStateProvider middleLayerProvider;
    public final BlockStateProvider outerLayerProvider;
    public final List<BlockState> innerPlacements;
    public final ResourceLocation cannotReplace;
    public final ResourceLocation invalidBlocks;
    public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("filling_provider")).forGetter(geodeBlockSettings -> geodeBlockSettings.fillingProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("inner_layer_provider")).forGetter(geodeBlockSettings -> geodeBlockSettings.innerLayerProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider")).forGetter(geodeBlockSettings -> geodeBlockSettings.alternateInnerLayerProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("middle_layer_provider")).forGetter(geodeBlockSettings -> geodeBlockSettings.middleLayerProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("outer_layer_provider")).forGetter(geodeBlockSettings -> geodeBlockSettings.outerLayerProvider), ((MapCodec)ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements")).forGetter(geodeBlockSettings -> geodeBlockSettings.innerPlacements), ((MapCodec)ResourceLocation.CODEC.fieldOf("cannot_replace")).forGetter(geodeBlockSettings -> geodeBlockSettings.cannotReplace), ((MapCodec)ResourceLocation.CODEC.fieldOf("invalid_blocks")).forGetter(geodeBlockSettings -> geodeBlockSettings.invalidBlocks)).apply((Applicative<GeodeBlockSettings, ?>)instance, GeodeBlockSettings::new));

    public GeodeBlockSettings(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, BlockStateProvider blockStateProvider3, BlockStateProvider blockStateProvider4, BlockStateProvider blockStateProvider5, List<BlockState> list, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        this.fillingProvider = blockStateProvider;
        this.innerLayerProvider = blockStateProvider2;
        this.alternateInnerLayerProvider = blockStateProvider3;
        this.middleLayerProvider = blockStateProvider4;
        this.outerLayerProvider = blockStateProvider5;
        this.innerPlacements = list;
        this.cannotReplace = resourceLocation;
        this.invalidBlocks = resourceLocation2;
    }
}

