/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface StructureTags {
    public static final TagKey<Structure> EYE_OF_ENDER_LOCATED = StructureTags.create("eye_of_ender_located");
    public static final TagKey<Structure> DOLPHIN_LOCATED = StructureTags.create("dolphin_located");
    public static final TagKey<Structure> ON_WOODLAND_EXPLORER_MAPS = StructureTags.create("on_woodland_explorer_maps");
    public static final TagKey<Structure> ON_OCEAN_EXPLORER_MAPS = StructureTags.create("on_ocean_explorer_maps");
    public static final TagKey<Structure> ON_TREASURE_MAPS = StructureTags.create("on_treasure_maps");
    public static final TagKey<Structure> CATS_SPAWN_IN = StructureTags.create("cats_spawn_in");
    public static final TagKey<Structure> CATS_SPAWN_AS_BLACK = StructureTags.create("cats_spawn_as_black");
    public static final TagKey<Structure> VILLAGE = StructureTags.create("village");
    public static final TagKey<Structure> MINESHAFT = StructureTags.create("mineshaft");
    public static final TagKey<Structure> SHIPWRECK = StructureTags.create("shipwreck");
    public static final TagKey<Structure> RUINED_PORTAL = StructureTags.create("ruined_portal");
    public static final TagKey<Structure> OCEAN_RUIN = StructureTags.create("ocean_ruin");

    private static TagKey<Structure> create(String string) {
        return TagKey.create(Registries.STRUCTURE, new ResourceLocation(string));
    }
}

