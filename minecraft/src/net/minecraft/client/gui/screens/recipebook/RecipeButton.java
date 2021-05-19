package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
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
	private static final float ANIMATION_TIME = 15.0F;
	private static final int BACKGROUND_SIZE = 25;
	public static final int TICKS_TO_SWAP = 30;
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
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
		int k = 29;
		if (!this.collection.hasCraftable()) {
			k += 25;
		}

		int l = 206;
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			l += 25;
		}

		boolean bl = this.animationTime > 0.0F;
		PoseStack poseStack2 = RenderSystem.getModelViewStack();
		if (bl) {
			float g = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
			poseStack2.pushPose();
			poseStack2.translate((double)(this.x + 8), (double)(this.y + 12), 0.0);
			poseStack2.scale(g, g, 1.0F);
			poseStack2.translate((double)(-(this.x + 8)), (double)(-(this.y + 12)), 0.0);
			RenderSystem.applyModelViewMatrix();
			this.animationTime -= f;
		}

		this.blit(poseStack, this.x, this.y, k, l, this.width, this.height);
		List<Recipe<?>> list = this.getOrderedRecipes();
		this.currentIndex = Mth.floor(this.time / 30.0F) % list.size();
		ItemStack itemStack = ((Recipe)list.get(this.currentIndex)).getResultItem();
		int m = 4;
		if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
			minecraft.getItemRenderer().renderAndDecorateItem(itemStack, this.x + m + 1, this.y + m + 1, 0, 10);
			m--;
		}

		minecraft.getItemRenderer().renderAndDecorateFakeItem(itemStack, this.x + m, this.y + m);
		if (bl) {
			poseStack2.popPose();
			RenderSystem.applyModelViewMatrix();
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
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		ItemStack itemStack = ((Recipe)this.getOrderedRecipes().get(this.currentIndex)).getResultItem();
		narrationElementOutput.add(NarratedElementType.TITLE, new TranslatableComponent("narration.recipe", itemStack.getHoverName()));
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			narrationElementOutput.add(
				NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"), new TranslatableComponent("narration.recipe.usage.more")
			);
		} else {
			narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"));
		}
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
