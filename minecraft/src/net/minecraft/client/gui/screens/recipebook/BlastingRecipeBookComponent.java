package net.minecraft.client.gui.screens.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Environment(EnvType.CLIENT)
public class BlastingRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
	@Override
	protected boolean getFilteringCraftable() {
		return this.book.isBlastingFurnaceFilteringCraftable();
	}

	@Override
	protected void setFilteringCraftable(boolean bl) {
		this.book.setBlastingFurnaceFilteringCraftable(bl);
	}

	@Override
	protected boolean isGuiOpen() {
		return this.book.isBlastingFurnaceGuiOpen();
	}

	@Override
	protected void setGuiOpen(boolean bl) {
		this.book.setBlastingFurnaceGuiOpen(bl);
	}

	@Override
	protected Component getRecipeFilterName() {
		return new TranslatableComponent("gui.recipebook.toggleRecipes.blastable");
	}

	@Override
	protected Set<Item> getFuelItems() {
		return AbstractFurnaceBlockEntity.getFuel().keySet();
	}
}
