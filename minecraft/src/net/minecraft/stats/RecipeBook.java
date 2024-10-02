package net.minecraft.stats;

import net.minecraft.world.inventory.RecipeBookType;

public class RecipeBook {
	protected final RecipeBookSettings bookSettings = new RecipeBookSettings();

	public boolean isOpen(RecipeBookType recipeBookType) {
		return this.bookSettings.isOpen(recipeBookType);
	}

	public void setOpen(RecipeBookType recipeBookType, boolean bl) {
		this.bookSettings.setOpen(recipeBookType, bl);
	}

	public boolean isFiltering(RecipeBookType recipeBookType) {
		return this.bookSettings.isFiltering(recipeBookType);
	}

	public void setFiltering(RecipeBookType recipeBookType, boolean bl) {
		this.bookSettings.setFiltering(recipeBookType, bl);
	}

	public void setBookSettings(RecipeBookSettings recipeBookSettings) {
		this.bookSettings.replaceFrom(recipeBookSettings);
	}

	public RecipeBookSettings getBookSettings() {
		return this.bookSettings.copy();
	}

	public void setBookSetting(RecipeBookType recipeBookType, boolean bl, boolean bl2) {
		this.bookSettings.setOpen(recipeBookType, bl);
		this.bookSettings.setFiltering(recipeBookType, bl2);
	}
}
