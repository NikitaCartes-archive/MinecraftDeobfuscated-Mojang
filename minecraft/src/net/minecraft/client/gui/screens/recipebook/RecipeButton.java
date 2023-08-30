package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeButton extends AbstractWidget {
	private static final ResourceLocation SLOT_MANY_CRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_many_craftable");
	private static final ResourceLocation SLOT_CRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_craftable");
	private static final ResourceLocation SLOT_MANY_UNCRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_many_uncraftable");
	private static final ResourceLocation SLOT_UNCRAFTABLE_SPRITE = new ResourceLocation("recipe_book/slot_uncraftable");
	private static final float ANIMATION_TIME = 15.0F;
	private static final int BACKGROUND_SIZE = 25;
	public static final int TICKS_TO_SWAP = 30;
	private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
	private RecipeBookMenu<?> menu;
	private RecipeBook book;
	private RecipeCollection collection;
	private float time;
	private float animationTime;
	private int currentIndex;

	public RecipeButton() {
		super(0, 0, 25, 25, CommonComponents.EMPTY);
	}

	public void init(RecipeCollection recipeCollection, RecipeBookPage recipeBookPage) {
		this.collection = recipeCollection;
		this.menu = (RecipeBookMenu<?>)recipeBookPage.getMinecraft().player.containerMenu;
		this.book = recipeBookPage.getRecipeBook();
		List<RecipeHolder<?>> list = recipeCollection.getRecipes(this.book.isFiltering(this.menu));

		for (RecipeHolder<?> recipeHolder : list) {
			if (this.book.willHighlight(recipeHolder)) {
				recipeBookPage.recipesShown(list);
				this.animationTime = 15.0F;
				break;
			}
		}
	}

	public RecipeCollection getCollection() {
		return this.collection;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (!Screen.hasControlDown()) {
			this.time += f;
		}

		ResourceLocation resourceLocation;
		if (this.collection.hasCraftable()) {
			if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
				resourceLocation = SLOT_MANY_CRAFTABLE_SPRITE;
			} else {
				resourceLocation = SLOT_CRAFTABLE_SPRITE;
			}
		} else if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			resourceLocation = SLOT_MANY_UNCRAFTABLE_SPRITE;
		} else {
			resourceLocation = SLOT_UNCRAFTABLE_SPRITE;
		}

		boolean bl = this.animationTime > 0.0F;
		if (bl) {
			float g = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
			guiGraphics.pose().scale(g, g, 1.0F);
			guiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
			this.animationTime -= f;
		}

		guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
		List<RecipeHolder<?>> list = this.getOrderedRecipes();
		this.currentIndex = Mth.floor(this.time / 30.0F) % list.size();
		ItemStack itemStack = ((RecipeHolder)list.get(this.currentIndex)).value().getResultItem(this.collection.registryAccess());
		int k = 4;
		if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
			guiGraphics.renderItem(itemStack, this.getX() + k + 1, this.getY() + k + 1, 0, 10);
			k--;
		}

		guiGraphics.renderFakeItem(itemStack, this.getX() + k, this.getY() + k);
		if (bl) {
			guiGraphics.pose().popPose();
		}
	}

	private List<RecipeHolder<?>> getOrderedRecipes() {
		List<RecipeHolder<?>> list = this.collection.getDisplayRecipes(true);
		if (!this.book.isFiltering(this.menu)) {
			list.addAll(this.collection.getDisplayRecipes(false));
		}

		return list;
	}

	public boolean isOnlyOption() {
		return this.getOrderedRecipes().size() == 1;
	}

	public RecipeHolder<?> getRecipe() {
		List<RecipeHolder<?>> list = this.getOrderedRecipes();
		return (RecipeHolder<?>)list.get(this.currentIndex);
	}

	public List<Component> getTooltipText() {
		ItemStack itemStack = ((RecipeHolder)this.getOrderedRecipes().get(this.currentIndex)).value().getResultItem(this.collection.registryAccess());
		List<Component> list = Lists.<Component>newArrayList(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack));
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			list.add(MORE_RECIPES_TOOLTIP);
		}

		return list;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		ItemStack itemStack = ((RecipeHolder)this.getOrderedRecipes().get(this.currentIndex)).value().getResultItem(this.collection.registryAccess());
		narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.recipe", itemStack.getHoverName()));
		if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
			narrationElementOutput.add(
				NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more")
			);
		} else {
			narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
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
