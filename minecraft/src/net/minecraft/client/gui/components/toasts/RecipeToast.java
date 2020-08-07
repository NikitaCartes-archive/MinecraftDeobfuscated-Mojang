package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeToast implements Toast {
	private static final Component TITLE_TEXT = new TranslatableComponent("recipe.toast.title");
	private static final Component DESCRIPTION_TEXT = new TranslatableComponent("recipe.toast.description");
	private final List<Recipe<?>> recipes = Lists.<Recipe<?>>newArrayList();
	private long lastChanged;
	private boolean changed;

	public RecipeToast(Recipe<?> recipe) {
		this.recipes.add(recipe);
	}

	@Override
	public Toast.Visibility render(PoseStack poseStack, ToastComponent toastComponent, long l) {
		if (this.changed) {
			this.lastChanged = l;
			this.changed = false;
		}

		if (this.recipes.isEmpty()) {
			return Toast.Visibility.HIDE;
		} else {
			toastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
			RenderSystem.color3f(1.0F, 1.0F, 1.0F);
			toastComponent.blit(poseStack, 0, 0, 0, 32, this.width(), this.height());
			toastComponent.getMinecraft().font.draw(poseStack, TITLE_TEXT, 30.0F, 7.0F, -11534256);
			toastComponent.getMinecraft().font.draw(poseStack, DESCRIPTION_TEXT, 30.0F, 18.0F, -16777216);
			Recipe<?> recipe = (Recipe<?>)this.recipes.get((int)(l / Math.max(1L, 5000L / (long)this.recipes.size()) % (long)this.recipes.size()));
			ItemStack itemStack = recipe.getToastSymbol();
			RenderSystem.pushMatrix();
			RenderSystem.scalef(0.6F, 0.6F, 1.0F);
			toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(itemStack, 3, 3);
			RenderSystem.popMatrix();
			toastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(recipe.getResultItem(), 8, 8);
			return l - this.lastChanged >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		}
	}

	private void addItem(Recipe<?> recipe) {
		this.recipes.add(recipe);
		this.changed = true;
	}

	public static void addOrUpdate(ToastComponent toastComponent, Recipe<?> recipe) {
		RecipeToast recipeToast = toastComponent.getToast(RecipeToast.class, NO_TOKEN);
		if (recipeToast == null) {
			toastComponent.addToast(new RecipeToast(recipe));
		} else {
			recipeToast.addItem(recipe);
		}
	}
}
