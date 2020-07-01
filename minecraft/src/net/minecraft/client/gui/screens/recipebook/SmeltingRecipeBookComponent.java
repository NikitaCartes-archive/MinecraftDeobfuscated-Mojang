package net.minecraft.client.gui.screens.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Environment(EnvType.CLIENT)
public class SmeltingRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
	@Override
	protected Component getRecipeFilterName() {
		return new TranslatableComponent("gui.recipebook.toggleRecipes.smeltable");
	}

	@Override
	protected Set<Item> getFuelItems() {
		return AbstractFurnaceBlockEntity.getFuel().keySet();
	}
}
