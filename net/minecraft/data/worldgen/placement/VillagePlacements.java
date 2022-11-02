/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class VillagePlacements {
    public static final ResourceKey<PlacedFeature> PILE_HAY_VILLAGE = PlacementUtils.createKey("pile_hay");
    public static final ResourceKey<PlacedFeature> PILE_MELON_VILLAGE = PlacementUtils.createKey("pile_melon");
    public static final ResourceKey<PlacedFeature> PILE_SNOW_VILLAGE = PlacementUtils.createKey("pile_snow");
    public static final ResourceKey<PlacedFeature> PILE_ICE_VILLAGE = PlacementUtils.createKey("pile_ice");
    public static final ResourceKey<PlacedFeature> PILE_PUMPKIN_VILLAGE = PlacementUtils.createKey("pile_pumpkin");
    public static final ResourceKey<PlacedFeature> OAK_VILLAGE = PlacementUtils.createKey("oak");
    public static final ResourceKey<PlacedFeature> ACACIA_VILLAGE = PlacementUtils.createKey("acacia");
    public static final ResourceKey<PlacedFeature> SPRUCE_VILLAGE = PlacementUtils.createKey("spruce");
    public static final ResourceKey<PlacedFeature> PINE_VILLAGE = PlacementUtils.createKey("pine");
    public static final ResourceKey<PlacedFeature> PATCH_CACTUS_VILLAGE = PlacementUtils.createKey("patch_cactus");
    public static final ResourceKey<PlacedFeature> FLOWER_PLAIN_VILLAGE = PlacementUtils.createKey("flower_plain");
    public static final ResourceKey<PlacedFeature> PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.createKey("patch_taiga_grass");
    public static final ResourceKey<PlacedFeature> PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.createKey("patch_berry_bush");

    public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstapContext.lookup(Registry.CONFIGURED_FEATURE_REGISTRY);
        Holder.Reference<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(PileFeatures.PILE_HAY);
        Holder.Reference<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(PileFeatures.PILE_MELON);
        Holder.Reference<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(PileFeatures.PILE_SNOW);
        Holder.Reference<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(PileFeatures.PILE_ICE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(PileFeatures.PILE_PUMPKIN);
        Holder.Reference<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(TreeFeatures.OAK);
        Holder.Reference<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(TreeFeatures.ACACIA);
        Holder.Reference<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(TreeFeatures.PINE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
        Holder.Reference<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
        Holder.Reference<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder.Reference<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
        PlacementUtils.register(bootstapContext, PILE_HAY_VILLAGE, holder, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PILE_MELON_VILLAGE, holder2, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PILE_SNOW_VILLAGE, holder3, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PILE_ICE_VILLAGE, holder4, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PILE_PUMPKIN_VILLAGE, holder5, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, OAK_VILLAGE, holder6, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
        PlacementUtils.register(bootstapContext, ACACIA_VILLAGE, holder7, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
        PlacementUtils.register(bootstapContext, SPRUCE_VILLAGE, holder8, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstapContext, PINE_VILLAGE, holder9, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
        PlacementUtils.register(bootstapContext, PATCH_CACTUS_VILLAGE, holder10, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, FLOWER_PLAIN_VILLAGE, holder11, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PATCH_TAIGA_GRASS_VILLAGE, holder12, new PlacementModifier[0]);
        PlacementUtils.register(bootstapContext, PATCH_BERRY_BUSH_VILLAGE, holder13, new PlacementModifier[0]);
    }
}

