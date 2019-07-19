package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBook {
	protected final Set<ResourceLocation> known = Sets.<ResourceLocation>newHashSet();
	protected final Set<ResourceLocation> highlight = Sets.<ResourceLocation>newHashSet();
	protected boolean guiOpen;
	protected boolean filteringCraftable;
	protected boolean furnaceGuiOpen;
	protected boolean furnaceFilteringCraftable;
	protected boolean blastingFurnaceGuiOpen;
	protected boolean blastingFurnaceFilteringCraftable;
	protected boolean smokerGuiOpen;
	protected boolean smokerFilteringCraftable;

	public void copyOverData(RecipeBook recipeBook) {
		this.known.clear();
		this.highlight.clear();
		this.known.addAll(recipeBook.known);
		this.highlight.addAll(recipeBook.highlight);
	}

	public void add(Recipe<?> recipe) {
		if (!recipe.isSpecial()) {
			this.add(recipe.getId());
		}
	}

	protected void add(ResourceLocation resourceLocation) {
		this.known.add(resourceLocation);
	}

	public boolean contains(@Nullable Recipe<?> recipe) {
		return recipe == null ? false : this.known.contains(recipe.getId());
	}

	@Environment(EnvType.CLIENT)
	public void remove(Recipe<?> recipe) {
		this.remove(recipe.getId());
	}

	protected void remove(ResourceLocation resourceLocation) {
		this.known.remove(resourceLocation);
		this.highlight.remove(resourceLocation);
	}

	@Environment(EnvType.CLIENT)
	public boolean willHighlight(Recipe<?> recipe) {
		return this.highlight.contains(recipe.getId());
	}

	public void removeHighlight(Recipe<?> recipe) {
		this.highlight.remove(recipe.getId());
	}

	public void addHighlight(Recipe<?> recipe) {
		this.addHighlight(recipe.getId());
	}

	protected void addHighlight(ResourceLocation resourceLocation) {
		this.highlight.add(resourceLocation);
	}

	@Environment(EnvType.CLIENT)
	public boolean isGuiOpen() {
		return this.guiOpen;
	}

	public void setGuiOpen(boolean bl) {
		this.guiOpen = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFilteringCraftable(RecipeBookMenu<?> recipeBookMenu) {
		if (recipeBookMenu instanceof FurnaceMenu) {
			return this.furnaceFilteringCraftable;
		} else if (recipeBookMenu instanceof BlastFurnaceMenu) {
			return this.blastingFurnaceFilteringCraftable;
		} else {
			return recipeBookMenu instanceof SmokerMenu ? this.smokerFilteringCraftable : this.filteringCraftable;
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean isFilteringCraftable() {
		return this.filteringCraftable;
	}

	public void setFilteringCraftable(boolean bl) {
		this.filteringCraftable = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFurnaceGuiOpen() {
		return this.furnaceGuiOpen;
	}

	public void setFurnaceGuiOpen(boolean bl) {
		this.furnaceGuiOpen = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isFurnaceFilteringCraftable() {
		return this.furnaceFilteringCraftable;
	}

	public void setFurnaceFilteringCraftable(boolean bl) {
		this.furnaceFilteringCraftable = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isBlastingFurnaceGuiOpen() {
		return this.blastingFurnaceGuiOpen;
	}

	public void setBlastingFurnaceGuiOpen(boolean bl) {
		this.blastingFurnaceGuiOpen = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isBlastingFurnaceFilteringCraftable() {
		return this.blastingFurnaceFilteringCraftable;
	}

	public void setBlastingFurnaceFilteringCraftable(boolean bl) {
		this.blastingFurnaceFilteringCraftable = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSmokerGuiOpen() {
		return this.smokerGuiOpen;
	}

	public void setSmokerGuiOpen(boolean bl) {
		this.smokerGuiOpen = bl;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSmokerFilteringCraftable() {
		return this.smokerFilteringCraftable;
	}

	public void setSmokerFilteringCraftable(boolean bl) {
		this.smokerFilteringCraftable = bl;
	}
}
