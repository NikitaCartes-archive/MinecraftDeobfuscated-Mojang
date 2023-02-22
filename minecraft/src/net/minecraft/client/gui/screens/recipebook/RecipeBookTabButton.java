package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeBookTabButton extends StateSwitchingButton {
	private final RecipeBookCategories category;
	private static final float ANIMATION_TIME = 15.0F;
	private float animationTime;

	public RecipeBookTabButton(RecipeBookCategories recipeBookCategories) {
		super(0, 0, 35, 27, false);
		this.category = recipeBookCategories;
		this.initTextureValues(153, 2, 35, 0, RecipeBookComponent.RECIPE_BOOK_LOCATION);
	}

	public void startAnimation(Minecraft minecraft) {
		ClientRecipeBook clientRecipeBook = minecraft.player.getRecipeBook();
		List<RecipeCollection> list = clientRecipeBook.getCollection(this.category);
		if (minecraft.player.containerMenu instanceof RecipeBookMenu) {
			for (RecipeCollection recipeCollection : list) {
				for (Recipe<?> recipe : recipeCollection.getRecipes(clientRecipeBook.isFiltering((RecipeBookMenu<?>)minecraft.player.containerMenu))) {
					if (clientRecipeBook.willHighlight(recipe)) {
						this.animationTime = 15.0F;
						return;
					}
				}
			}
		}
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		if (this.animationTime > 0.0F) {
			float g = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
			poseStack.pushPose();
			poseStack.translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
			poseStack.scale(1.0F, g, 1.0F);
			poseStack.translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
		}

		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, this.resourceLocation);
		RenderSystem.disableDepthTest();
		int k = this.xTexStart;
		int l = this.yTexStart;
		if (this.isStateTriggered) {
			k += this.xDiffTex;
		}

		if (this.isHoveredOrFocused()) {
			l += this.yDiffTex;
		}

		int m = this.getX();
		if (this.isStateTriggered) {
			m -= 2;
		}

		blit(poseStack, m, this.getY(), k, l, this.width, this.height);
		RenderSystem.enableDepthTest();
		this.renderIcon(poseStack, minecraft.getItemRenderer());
		if (this.animationTime > 0.0F) {
			poseStack.popPose();
			this.animationTime -= f;
		}
	}

	private void renderIcon(PoseStack poseStack, ItemRenderer itemRenderer) {
		List<ItemStack> list = this.category.getIconItems();
		int i = this.isStateTriggered ? -2 : 0;
		if (list.size() == 1) {
			itemRenderer.renderAndDecorateFakeItem(poseStack, (ItemStack)list.get(0), this.getX() + 9 + i, this.getY() + 5);
		} else if (list.size() == 2) {
			itemRenderer.renderAndDecorateFakeItem(poseStack, (ItemStack)list.get(0), this.getX() + 3 + i, this.getY() + 5);
			itemRenderer.renderAndDecorateFakeItem(poseStack, (ItemStack)list.get(1), this.getX() + 14 + i, this.getY() + 5);
		}
	}

	public RecipeBookCategories getCategory() {
		return this.category;
	}

	public boolean updateVisibility(ClientRecipeBook clientRecipeBook) {
		List<RecipeCollection> list = clientRecipeBook.getCollection(this.category);
		this.visible = false;
		if (list != null) {
			for (RecipeCollection recipeCollection : list) {
				if (recipeCollection.hasKnownRecipes() && recipeCollection.hasFitting()) {
					this.visible = true;
					break;
				}
			}
		}

		return this.visible;
	}
}
