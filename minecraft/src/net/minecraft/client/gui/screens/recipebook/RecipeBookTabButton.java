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
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		if (this.animationTime > 0.0F) {
			float g = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
			RenderSystem.pushMatrix();
			RenderSystem.translatef((float)(this.x + 8), (float)(this.y + 12), 0.0F);
			RenderSystem.scalef(1.0F, g, 1.0F);
			RenderSystem.translatef((float)(-(this.x + 8)), (float)(-(this.y + 12)), 0.0F);
		}

		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(this.resourceLocation);
		RenderSystem.disableDepthTest();
		int k = this.xTexStart;
		int l = this.yTexStart;
		if (this.isStateTriggered) {
			k += this.xDiffTex;
		}

		if (this.isHovered()) {
			l += this.yDiffTex;
		}

		int m = this.x;
		if (this.isStateTriggered) {
			m -= 2;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.blit(poseStack, m, this.y, k, l, this.width, this.height);
		RenderSystem.enableDepthTest();
		this.renderIcon(minecraft.getItemRenderer());
		if (this.animationTime > 0.0F) {
			RenderSystem.popMatrix();
			this.animationTime -= f;
		}
	}

	private void renderIcon(ItemRenderer itemRenderer) {
		List<ItemStack> list = this.category.getIconItems();
		int i = this.isStateTriggered ? -2 : 0;
		if (list.size() == 1) {
			itemRenderer.renderAndDecorateFakeItem((ItemStack)list.get(0), this.x + 9 + i, this.y + 5);
		} else if (list.size() == 2) {
			itemRenderer.renderAndDecorateFakeItem((ItemStack)list.get(0), this.x + 3 + i, this.y + 5);
			itemRenderer.renderAndDecorateFakeItem((ItemStack)list.get(1), this.x + 14 + i, this.y + 5);
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
