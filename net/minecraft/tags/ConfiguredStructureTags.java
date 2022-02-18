/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

public interface ConfiguredStructureTags {
    public static final TagKey<ConfiguredStructureFeature<?, ?>> EYE_OF_ENDER_LOCATED = ConfiguredStructureTags.create("eye_of_ender_located");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> DOLPHIN_LOCATED = ConfiguredStructureTags.create("dolphin_located");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> ON_WOODLAND_EXPLORER_MAPS = ConfiguredStructureTags.create("on_woodland_explorer_maps");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> ON_OCEAN_EXPLORER_MAPS = ConfiguredStructureTags.create("on_ocean_explorer_maps");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> ON_TREASURE_MAPS = ConfiguredStructureTags.create("on_treasure_maps");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> VILLAGE = ConfiguredStructureTags.create("village");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> MINESHAFT = ConfiguredStructureTags.create("mineshaft");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> SHIPWRECK = ConfiguredStructureTags.create("shipwreck");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> RUINED_PORTAL = ConfiguredStructureTags.create("ruined_portal");
    public static final TagKey<ConfiguredStructureFeature<?, ?>> OCEAN_RUIN = ConfiguredStructureTags.create("ocean_ruin");

    private static TagKey<ConfiguredStructureFeature<?, ?>> create(String string) {
        return TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(string));
    }
}

