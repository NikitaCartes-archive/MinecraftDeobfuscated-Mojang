package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeBook {
	protected final Set<ResourceLocation> known = Sets.<ResourceLocation>newHashSet();
	protected final Set<ResourceLocation> highlight = Sets.<ResourceLocation>newHashSet();
	private final RecipeBookSettings bookSettings = new RecipeBookSettings();

	public void copyOverData(RecipeBook recipeBook) {
		this.known.clear();
		this.highlight.clear();
		this.bookSettings.replaceFrom(recipeBook.bookSettings);
		this.known.addAll(recipeBook.known);
		this.highlight.addAll(recipeBook.highlight);
	}

	public void add(RecipeHolder<?> recipeHolder) {
		if (!recipeHolder.value().isSpecial()) {
			this.add(recipeHolder.id());
		}
	}

	protected void add(ResourceLocation resourceLocation) {
		this.known.add(resourceLocation);
	}

	public boolean contains(@Nullable RecipeHolder<?> recipeHolder) {
		return recipeHolder == null ? false : this.known.contains(recipeHolder.id());
	}

	public boolean contains(ResourceLocation resourceLocation) {
		return this.known.contains(resourceLocation);
	}

	public void remove(RecipeHolder<?> recipeHolder) {
		this.remove(recipeHolder.id());
	}

	protected void remove(ResourceLocation resourceLocation) {
		this.known.remove(resourceLocation);
		this.highlight.remove(resourceLocation);
	}

	public boolean willHighlight(RecipeHolder<?> recipeHolder) {
		return this.highlight.contains(recipeHolder.id());
	}

	public void removeHighlight(RecipeHolder<?> recipeHolder) {
		this.highlight.remove(recipeHolder.id());
	}

	public void addHighlight(RecipeHolder<?> recipeHolder) {
		this.addHighlight(recipeHolder.id());
	}

	protected void addHighlight(ResourceLocation resourceLocation) {
		this.highlight.add(resourceLocation);
	}

	public boolean isOpen(RecipeBookType recipeBookType) {
		return this.bookSettings.isOpen(recipeBookType);
	}

	public void setOpen(RecipeBookType recipeBookType, boolean bl) {
		this.bookSettings.setOpen(recipeBookType, bl);
	}

	public boolean isFiltering(RecipeBookMenu<?> recipeBookMenu) {
		return this.isFiltering(recipeBookMenu.getRecipeBookType());
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
