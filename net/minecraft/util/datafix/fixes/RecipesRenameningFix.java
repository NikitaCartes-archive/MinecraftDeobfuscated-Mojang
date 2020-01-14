/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.util.datafix.fixes.RecipesRenameFix;

public class RecipesRenameningFix
extends RecipesRenameFix {
    private static final Map<String, String> RECIPES = ImmutableMap.builder().put("minecraft:acacia_bark", "minecraft:acacia_wood").put("minecraft:birch_bark", "minecraft:birch_wood").put("minecraft:dark_oak_bark", "minecraft:dark_oak_wood").put("minecraft:jungle_bark", "minecraft:jungle_wood").put("minecraft:oak_bark", "minecraft:oak_wood").put("minecraft:spruce_bark", "minecraft:spruce_wood").build();

    public RecipesRenameningFix(Schema schema, boolean bl) {
        super(schema, bl, "Recipes renamening fix", string -> RECIPES.getOrDefault(string, (String)string));
    }
}

