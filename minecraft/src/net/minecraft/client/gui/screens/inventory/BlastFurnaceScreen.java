package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

@Environment(EnvType.CLIENT)
public class BlastFurnaceScreen extends AbstractFurnaceScreen<BlastFurnaceMenu> {
	private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/blast_furnace/lit_progress");
	private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/blast_furnace/burn_progress");
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/blast_furnace.png");
	private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.blastable");
	private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
		new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.BLAST_FURNACE),
		new RecipeBookComponent.TabInfo(Items.REDSTONE_ORE, RecipeBookCategories.BLAST_FURNACE_BLOCKS),
		new RecipeBookComponent.TabInfo(Items.IRON_SHOVEL, Items.GOLDEN_LEGGINGS, RecipeBookCategories.BLAST_FURNACE_MISC)
	);

	public BlastFurnaceScreen(BlastFurnaceMenu blastFurnaceMenu, Inventory inventory, Component component) {
		super(blastFurnaceMenu, inventory, component, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
	}
}
