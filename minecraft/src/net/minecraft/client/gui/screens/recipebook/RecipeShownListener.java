package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public interface RecipeShownListener {
	void recipesShown(List<RecipeHolder<?>> list);
}
