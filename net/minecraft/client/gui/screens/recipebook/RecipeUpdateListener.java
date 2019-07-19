/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

@Environment(value=EnvType.CLIENT)
public interface RecipeUpdateListener {
    public void recipesUpdated();

    public RecipeBookComponent getRecipeBookComponent();
}

