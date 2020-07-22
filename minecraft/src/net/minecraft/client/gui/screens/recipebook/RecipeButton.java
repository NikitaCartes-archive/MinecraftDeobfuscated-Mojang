package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeButton extends AbstractWidget {
	private static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
	private static final Component MORE_RECIPES_TOOLTIP = new TranslatableComponent("gui.recipebook.moreRecipes");
	private RecipeBookMenu<?> menu;
	private RecipeBook book;
	private RecipeCollection collection;
	private float time;
	private float animationTime;
	private int currentIndex;

	public RecipeButton() {
		super(0, 0, 25, 25, TextComponent.EMPTY);
	}

	public void init(RecipeCollection recipeCollection, RecipeBookPage recipeBookPage) {
		this.collection = recipeCollection;
		this.menu = (RecipeBookMenu<?>)recipeBookPage.getMinecraft().player.containerMenu;
		this.book = recipeBookPage.getRecipeBook();
		List<Recipe<?>> list = recipeCollection.getRecipes(this.book.isFiltering(this.menu));

		for (Recipe<?> recipe : list) {
			if (this.book.willHighlight(recipe)) {
				recipeBookPage.recipesShown(list);
				this.animationTime = 15.0F;
				break;
			}
		}
	}

	public RecipeCollection getCollection() {
		return this.collection;
	}

	public void setPosition(int i, int j) {
		this.x = i;
		this.y = j;
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		if (!Screen.hasControlDown()) {
			this.time += f;
		}

		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(RECIPE_BOOK_LOCATION);
		int k = 29;
		if (!this.collection.hasCraftable()) {
			k += 25;
		}

		int l = 206;
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			l += 25;
		}

		boolean bl = this.animationTime > 0.0F;
		if (bl) {
			float g = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)(this.x + 8), (float)(this.y + 12), 0.0F);
			RenderSystem.scalef(g, g, 1.0F);
			RenderSystem.translatef((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
			this.animationTime -= f;
		}

		this.blit(poseStack, this.x, this.y, k, l, this.width, this.height);
		List<Recipe<?>> list = this.getOrderedRecipes();
		this.currentIndex = Mth.floor(this.time / 30.0F) % list.size();
		ItemStack itemStack = ((Recipe)list.get(this.currentIndex)).getResultItem();
		int m = 4;
		if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
			minecraft.getItemRenderer().renderAndDecorateItem(itemStack, this.x + m + 1, this.y + m + 1);
			m--;
		}

		minecraft.getItemRenderer().renderAndDecorateFakeItem(itemStack, this.x + m, this.y + m);
		if (bl) {
			RenderSystem.popMatrix();
		}
	}

	private List<Recipe<?>> getOrderedRecipes() {
		List<Recipe<?>> list = this.collection.getDisplayRecipes(true);
		if (!this.book.isFiltering(this.menu)) {
			list.addAll(this.collection.getDisplayRecipes(false));
		}

		return list;
	}

	public boolean isOnlyOption() {
		return this.getOrderedRecipes().size() == 1;
	}

	public Recipe<?> getRecipe() {
		List<Recipe<?>> list = this.getOrderedRecipes();
		return (Recipe<?>)list.get(this.currentIndex);
	}

	public List<Component> getTooltipText(Screen screen) {
		ItemStack itemStack = ((Recipe)this.getOrderedRecipes().get(this.currentIndex)).getResultItem();
		List<Component> list = Lists.<Component>newArrayList(screen.getTooltipFromItem(itemStack));
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			list.add(MORE_RECIPES_TOOLTIP);
		}

		return list;
	}

	@Override
	public int getWidth() {
		return 25;
	}

	@Override
	protected boolean isValidClickButton(int i) {
		return i == 0 || i == 1;
	}
}
