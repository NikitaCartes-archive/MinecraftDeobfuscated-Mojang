/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.Recipe;

@Environment(value=EnvType.CLIENT)
public interface RecipeShownListener {
    public void recipesShown(List<Recipe<?>> var1);
}

