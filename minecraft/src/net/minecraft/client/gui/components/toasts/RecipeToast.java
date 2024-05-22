package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");
	private static final long DISPLAY_TIME = 5000L;
	private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
	private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
	private final List<RecipeHolder<?>> recipes = Lists.<RecipeHolder<?>>newArrayList();
	private long lastChanged;
	private boolean changed;

	public RecipeToast(RecipeHolder<?> recipeHolder) {
		this.recipes.add(recipeHolder);
	}

	@Override
	public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
		if (this.changed) {
			this.lastChanged = l;
			this.changed = false;
		}

		if (this.recipes.isEmpty()) {
			return Toast.Visibility.HIDE;
		} else {
			guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
			guiGraphics.drawString(toastComponent.getMinecraft().font, TITLE_TEXT, 30, 7, -11534256, false);
			guiGraphics.drawString(toastComponent.getMinecraft().font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
			RecipeHolder<?> recipeHolder = (RecipeHolder<?>)this.recipes
				.get(
					(int)(
						(double)l / Math.max(1.0, 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size()) % (double)this.recipes.size()
					)
				);
			ItemStack itemStack = recipeHolder.value().getToastSymbol();
			guiGraphics.pose().pushPose();
			guiGraphics.pose().scale(0.6F, 0.6F, 1.0F);
			guiGraphics.renderFakeItem(itemStack, 3, 3);
			guiGraphics.pose().popPose();
			guiGraphics.renderFakeItem(recipeHolder.value().getResultItem(toastComponent.getMinecraft().level.registryAccess()), 8, 8);
			return (double)(l - this.lastChanged) >= 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		}
	}

	private void addItem(RecipeHolder<?> recipeHolder) {
		this.recipes.add(recipeHolder);
		this.changed = true;
	}

	public static void addOrUpdate(ToastComponent toastComponent, RecipeHolder<?> recipeHolder) {
		RecipeToast recipeToast = toastComponent.getToast(RecipeToast.class, NO_TOKEN);
		if (recipeToast == null) {
			toastComponent.addToast(new RecipeToast(recipeHolder));
		} else {
			recipeToast.addItem(recipeHolder);
		}
	}
}
